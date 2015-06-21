(function() {
  var app = angular.module('iotAnyware', ['ui.bootstrap'])
  .controller('SAnodeCtrl', ['$log', '$scope', '$http', function($log, $scope, $http) {
      $scope.user = 'simpson.homer@gmail.com'
      $scope.token = 'cc7b3c54c35eacac';
      $scope.clientId = '75f9e675-9db4-4d02-b523-37521ef656ea'

      var self = this;
      self.nodes = {}; 

      client = new Paho.MQTT.Client('54.166.26.101', 8080, '/', 'myclientid_' + parseInt(Math.random() * 100, 10))
      client.onMessageArrived = onMessageArrived;
      client.connect({onSuccess:onConnect});

      self.highlight = function(id) {
        angular.element(document. querySelector(id)).addClass('status-changed');
        setTimeout(function() {
            angular.element(document. querySelector(id)).removeClass('status-changed');
        }, 800);
      };

      function onMessageArrived(message) {
        var nodeId = message.destinationName.split('/')[2];
        if (self.nodes[nodeId]){
            angular.forEach(JSON.parse(message.payloadString), function(val, key) {
                self.nodes[nodeId].status[key] = val;
                self.highlight('#'+nodeId+'-'+key);
            });
            $scope.$apply();
        }
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
     }
  }])
  .config(function($interpolateProvider) {
      $interpolateProvider.startSymbol('[[');
      $interpolateProvider.endSymbol(']]');
  });
})();
