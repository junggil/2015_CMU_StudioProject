(function() {
  var app = angular.module('iotAnyware', ['ui.bootstrap'])
  .controller('SAnodeCtrl', ['$log', '$scope', '$http', function($log, $scope, $http) {
      $scope.user = 'simpson.homer@gmail.com'
      $scope.token = 'cc7b3c54c35eacac';
      $scope.clientId = '75f9e675-9db4-4d02-b523-37521ef656ea'

      var self = this;
      self.nodes = {}; 
      self.logs = [];

      $scope.currentPage = 1;
      $scope.pageSize = 15;

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

      $scope.isActiveValue = function(val) {
          return $.inArray(val, ['off', 'close', 'vacant']) < 0;
      };

      function onMessageArrived(message) {
        var nodeId = message.destinationName.split('/')[2];
        if (self.nodes[nodeId]){
            angular.forEach(JSON.parse(message.payloadString), function(val, key) {
                self.nodes[nodeId].status[key] = val;
            });
            self.logs.unshift({timestamp: (new Date()).strftime('%B %d %H:%M:%S'), node: nodeId,
                               msg_type: message.destinationName.split('/')[3], msg: message.payloadString});
            $scope.$apply();
        }
        console.log(message.payloadString);
      }

      function onConnect() {
        $http({url:'/user/getNodeList?session=' + $scope.token, method:'GET', 
                    headers: {'Content-type': 'application/json', 'x-client-id': $scope.clientId}}).then(function (res) {
            if (res.data.statusCode === 200) {
                angular.forEach(res.data.result, function(nodeInfo) {
                    nodeInfo.status = {};
                    delete nodeInfo.profiles;
                    self.nodes[nodeInfo.node] = nodeInfo;
                    client.subscribe('/sanode/'+nodeInfo.node+'/status');
                    message = new Paho.MQTT.Message(JSON.stringify({publisher:$scope.user}));
                    message.destinationName = '/sanode/'+nodeInfo.node+'/query';
                    client.send(message); 
                });
            }
        });
        $http({url:'/log/getHistory?session=' + $scope.token, method:'GET', 
                    headers: {'Content-type': 'application/json', 'x-client-id': $scope.clientId}}).then(function (res) {
            if (res.data.statusCode === 200) {
                self.logs = res.data.result;
                console.log(self.logs.length);
            }
        });
     }
  }])
  .config(function($interpolateProvider) {
      $interpolateProvider.startSymbol('[[');
      $interpolateProvider.endSymbol(']]');
  })
  .filter('startFrom', function() {
    return function(input, start) {
        start = +start; //parse to int
        return input.slice(start);
    }
});

})();
