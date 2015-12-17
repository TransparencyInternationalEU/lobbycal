'use strict';

angular.module('lobbycalApp')
    .controller('TagDetailController', function ($scope, $stateParams, Tag, Meeting) {
        $scope.tag = {};
        $scope.load = function (id) {
            Tag.get({id: id}, function(result) {
              $scope.tag = result;
            });
        };
        $scope.load($stateParams.id);
    });
