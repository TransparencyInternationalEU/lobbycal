'use strict';

angular.module('lobbycalApp')
    .controller('AliasDetailController', function ($scope, $stateParams, Alias, User) {
        $scope.alias = {};
        $scope.load = function (id) {
            Alias.get({id: id}, function(result) {
              $scope.alias = result;
            });
        };
        $scope.load($stateParams.id);
    });
