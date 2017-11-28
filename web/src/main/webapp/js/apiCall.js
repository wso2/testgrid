angular.module('demo', []).controller('producttestplansctrl', function($scope, $http) {
    $scope.producttestplans = [];
    $http.get('api/producttestplan/all').
        then(function(response) {
            $scope.producttestplans = response.data;
        });
});