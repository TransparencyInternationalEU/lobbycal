'use strict';

angular.module('lobbycalApp')
    .controller('PartnerDetailController', function ($scope, $stateParams, Partner, Meeting) {
        $scope.partner = {};
        $scope.load = function (id) {
            Partner.get({id: id}, function(result) {
              $scope.partner = result;
            });
        };
        $scope.load($stateParams.id);
    });
