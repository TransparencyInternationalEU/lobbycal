'use strict';

angular.module('lobbycalApp')
    .controller('SubmitterDetailController', function ($scope, $stateParams, Submitter, User) {
        $scope.submitter = {};
        $scope.load = function (id) {
            Submitter.get({id: id}, function(result) {
              $scope.submitter = result;
            });
        };
        $scope.load($stateParams.id);
    });
