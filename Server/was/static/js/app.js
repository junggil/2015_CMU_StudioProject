(function() {
  var app = angular.module('iotAnyware', ['ui.bootstrap', 'ui-notification'])
  .controller('SAnodeCtrl', ['$log', '$scope', '$http', 'Notification', function($log, $scope, $http, Notification) {
      $scope.user = 'simpson.homer@gmail.com'
      $scope.token = 'cc7b3c54c35eacac';
      $scope.clientId = '75f9e675-9db4-4d02-b523-37521ef656ea'

      var self = this;
      self.nodes = {}; 
      self.logs = [];

      $scope.currentPage = 1;
      $scope.pageSize = 15;
      $scope.registerNodeInfo = {virtual: 'Use Real Device'};
      $scope.isCollapsed = {};
      $scope.alertMsg = '';
      $scope.nodeConfig = '';

      client = new Paho.MQTT.Client('54.166.26.101', 8080, '/', 'myclientid_' + parseInt(Math.random() * 100, 10))
      client.onMessageArrived = onMessageArrived;
      client.connect({onSuccess:onConnect});

      $scope.isEmptyObject = function(obj) {
          return Object.keys(obj).length == 0;
      };

      $scope.getIconName = function(key, val) {
          var icon = {light: 'wb_incandescent', door: 'lock_open', thermostat: 'equalizer', 
                      proximity: 'place', humidity: 'loyalty', alarm: 'security'}[key] || 'check';
          return val == 'close' ? 'lock' : icon;
      };
      
      $scope.toggleSwitch = function(node, key, val) {
          if ($.inArray(key, ['door', 'light', 'alarm']) >= 0) {
                var toggled_val = {open: 'close', close: 'open', on: 'off', off: 'on'}
                message = new Paho.MQTT.Message(JSON.stringify({publisher: $scope.user, name: key, value: toggled_val[val]}));
                message.destinationName = '/sanode/'+node+'/control';
                client.send(message); 
          }
      };

      $scope.shareNode = function(nodeId) {
          $http({url:'/user/shareNode', method:'POST', 
              data:{session:$scope.token, nodeId:nodeId, targetUser:$scope.nodeConfig(nodeId)},
              headers: {'Content-type': 'application/json', 'x-client-id': $scope.clientId}}).then(function (res) {
                  if (res.data.statusCode === 200) {
                      Notification.success('Successed!');
                      $scope.toggleCollapse(nodeId);
                  } else {
                      Notification.error(res.data.result);
                  }
              });
      }

      $scope.transferNode = function(nodeId) {
          $http({url:'/user/transferNode', method:'POST', 
              data:{session:$scope.token, nodeId:nodeId, targetUser:$scope.nodeConfig(nodeId)},
              headers: {'Content-type': 'application/json', 'x-client-id': $scope.clientId}}).then(function (res) {
                  if (res.data.statusCode === 200) {
                      Notification.success('Successed!');
                      $scope.toggleCollapse(nodeId);
                  } else {
                      console.log(res.data.result);
                      Notification.error(res.data.result);
                  }
              });
      }

      $scope.unregisterNode = function(nodeId) {
          $http({url:'/user/unregisterNode', method:'POST', 
              data:{session:$scope.token, nodeId:nodeId},
              headers: {'Content-type': 'application/json', 'x-client-id': $scope.clientId}}).then(function (res) {
                  if (res.data.statusCode === 200) {
                      delete self.nodes[nodeId];
                      $scope.refreshLog();
                  } else {
                      console.log(res.data.result);
                      Notification.error(res.data.result);
                  }
              });
      }

      $scope.checkCollapse = function(nodeId) {
          return $scope.isCollapsed[nodeId];
      }

      $scope.toggleCollapse = function(nodeId) {
          $scope.isCollapsed[nodeId] = !$scope.isCollapsed[nodeId];
      }

      $scope.registerNode = function() {
          $http({url:'/user/registerNode', method:'POST', 
              data:{session:$scope.token, nodeId:$scope.registerNodeInfo.nodeId, 
                    nickName:$scope.registerNodeInfo.nickName, virtual: $scope.registerNodeInfo.virtual === 'Use Virtual Device'},
              headers: {'Content-type': 'application/json', 'x-client-id': $scope.clientId}}).then(function (res) {
                  if (res.data.statusCode === 200) {
                      $('#registerModal .close').click();
                      $scope.registerNodeInfo = {virtual:'Use Real Device'};
                      $scope.alertMsg = '';
                  } else {
                      console.log(res.data.result);
                      $scope.alertMsg = res.data.result;
                      $('#registerNodeId').focus();
                  }
              });
      };

      $scope.isActiveValue = function(val) {
          return $.inArray(val, ['off', 'close', 'vacant']) < 0;
      };

      $scope.refreshLog = function() {
        $http({url:'/log/getHistory?session=' + $scope.token, method:'GET', 
                    headers: {'Content-type': 'application/json', 'x-client-id': $scope.clientId}}).then(function (res) {
            if (res.data.statusCode === 200) {
                self.logs = res.data.result;
            }
        });
      }

      function onMessageArrived(message) {
        var values = message.destinationName.split('/');
        var nodeId = values[2];
        var topicType = values[3];
        var payload = JSON.parse(message.payloadString);

        if (topicType == 'register') {
            nodeId = payload.node;
            self.nodes[nodeId] = payload;
            self.nodes[nodeId].status = {};
            client.subscribe('/sanode/'+nodeId+'/status');
            client.subscribe('/sanode/'+nodeId+'/notify');
            message = new Paho.MQTT.Message(JSON.stringify({publisher:$scope.user}));
            message.destinationName = '/sanode/'+nodeId+'/query';
            client.send(message); 
            $scope.refreshLog();
        } else if (topicType == 'status') {
            if (self.nodes[nodeId]){
                angular.forEach(payload, function(val, key) {
                    if ($.inArray(key, ['autoalarmon', 'autolightoff']) < 0) {
                        self.nodes[nodeId].status[key] = val;
                    }
                });
                self.logs.unshift({timestamp: (new Date()).strftime('%B %d %H:%M:%S'), node: nodeId,
                    msg_type: message.destinationName.split('/')[3], msg: message.payloadString});
                $scope.$apply();
            }
        } else if (topicType == 'notify') {
            if (payload.type === 'toast') {
                Notification.success(payload.warn);
            } else {
                Notification.warning({message: payload.info});
            }
        } else if (topicType == 'heartbeat') {
            delete self.nodes[nodeId];
            $scope.$apply();
        }
      }

      function onConnect() {
        client.subscribe('/user/'+$scope.user+'/register');
        $http({url:'/user/getNodeList?session=' + $scope.token, method:'GET', 
                    headers: {'Content-type': 'application/json', 'x-client-id': $scope.clientId}}).then(function (res) {
            if (res.data.statusCode === 200) {
                angular.forEach(res.data.result, function(nodeInfo) {
                    nodeInfo.status = {};
                    delete nodeInfo.profiles;
                    self.nodes[nodeInfo.node] = nodeInfo;
                    client.subscribe('/sanode/'+nodeInfo.node+'/status');
                    client.subscribe('/sanode/'+nodeInfo.node+'/notify');
                    message = new Paho.MQTT.Message(JSON.stringify({publisher:$scope.user}));
                    message.destinationName = '/sanode/'+nodeInfo.node+'/query';
                    client.send(message); 
                });
            }
        });
        $scope.refreshLog();
     }
  }])
  .config(function($interpolateProvider) {
      $interpolateProvider.startSymbol('[[');
      $interpolateProvider.endSymbol(']]');
  })
  .config(function(NotificationProvider) {
      NotificationProvider.setOptions({
          delay: 5000,
          startTop: 20,
          startRight: 10,
          verticalSpacing: 20,
          horizontalSpacing: 20,
          positionX: 'right',
          positionY: 'top'
      });
  })
  .filter('startFrom', function() {
    return function(input, start) {
        start = +start; //parse to int
        return input.slice(start);
    }
  });
})();
