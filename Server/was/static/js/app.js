(function() {
  var app = angular.module('iotAnyware', ['ui.bootstrap', 'ui-notification'])
  .controller('SAnodeCtrl', ['$log', '$scope', '$http', 'Notification', function($log, $scope, $http, Notification) {

      var self = this;
      self.nodes = {}; 
      self.logs = [];
      self.members = [
          {name: 'Jangsu Lee',    img: 'member1.gif', tooltip: 'Team Leader / Managing Engineer'},
          {name: 'Yoonki Hong',   img: 'member2.gif', tooltip: 'Requirements Eng. / Technical Writer'},
          {name: 'Jeonggil Lee',  img: 'member3.gif', tooltip: 'Chief Architect / Support Engineer'},
          {name: 'Changwook Lim', img: 'member4.gif', tooltip: 'Developer'},
          {name: 'Jaehoon Kim',   img: 'member5.gif', tooltip: 'Developer'},
      ]

      $scope.clientId = '75f9e675-9db4-4d02-b523-37521ef656ea'
      $scope.currentPage = 1;
      $scope.pageSize = 15;
      $scope.registerNodeInfo = {virtual: 'Use Real Device'};
      $scope.loginInfo = {};
      $scope.signupInfo = {};
      $scope.isCollapsed = {};
      $scope.alertMsg = {};
      $scope.nodeConfig = '';

      client = new Paho.MQTT.Client('54.166.26.101', 8080, '/', 'myclientid_' + parseInt(Math.random() * 100, 10))
      client.onMessageArrived = onMessageArrived;
      client.connect();

      $scope.isLoggedIn = function() {
          return $scope.token !== undefined;
      };

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

      $scope.signup = function() {
          $http({url:'/account/registerNewUser', method:'POST', 
              data:{email:$scope.signupInfo.email, password:$scope.signupInfo.password, nickName: $scope.signupInfo.name},
              headers: {'Content-type': 'application/json', 'x-client-id': $scope.clientId}}).then(function (res) {
                  if (res.data.statusCode === 200) {
                      $scope.alertMsg = {};
                      Notification.success({message:'Registeration Success. <br> Please check your mail box'});
                      $('#signupModal .close').click();
                  } else {
                      console.log(res.data.result);
                      $scope.alertMsg.signup = res.data.result;
                  }
              });
      };

      $scope.logout = function() {
          $scope.token = undefined;
          $scope.user = undefined;
          self.nodes = {};
          self.logs = [];
      }

      $scope.login = function() {
          $http({url:'/session/createUser', method:'POST', 
              data:{email:$scope.loginInfo.email, password:$scope.loginInfo.password},
              headers: {'Content-type': 'application/json', 'x-client-id': $scope.clientId}}).then(function (res) {
                  if (res.data.statusCode === 200) {
                      $scope.alertMsg = {};
                      $scope.user = $scope.loginInfo.email;
                      $scope.token = res.data.result.session;
                      $scope.init();
                      $('#loginModal .close').click();
                      $scope.loginInfo = {};
                  } else {
                      console.log(res.data.result);
                      $scope.alertMsg.login = res.data.result;
                  }
              });
      };

      $scope.registerNode = function() {
          $http({url:'/user/registerNode', method:'POST', 
              data:{session:$scope.token, nodeId:$scope.registerNodeInfo.nodeId, 
                    nickName:$scope.registerNodeInfo.nickName, virtual: $scope.registerNodeInfo.virtual === 'Use Virtual Device'},
              headers: {'Content-type': 'application/json', 'x-client-id': $scope.clientId}}).then(function (res) {
                  if (res.data.statusCode === 200) {
                      $('#registerModal .close').click();
                      $scope.registerNodeInfo = {virtual:'Use Real Device'};
                      $scope.alertMsg = {};
                  } else {
                      console.log(res.data.result);
                      $scope.alertMsg.reg = res.data.result;
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
                Notification.success(payload.info || payload.warn);
            } else {
                Notification.warning(payload.info || payload.warn);
            }
        } else if (topicType == 'heartbeat') {
            delete self.nodes[nodeId];
            $scope.$apply();
        }
      }

      $scope.init = function() {
        client.subscribe('/user/'+$scope.user+'/register');
        $http({url:'/user/getNodeList?session=' + $scope.token, method:'GET', 
                    headers: {'Content-type': 'application/json', 'x-client-id': $scope.clientId}}).then(function (res) {
            if (res.data.statusCode === 200) {
                console.log(res.data.result);
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

     $('html body').scrollTop(0);
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
