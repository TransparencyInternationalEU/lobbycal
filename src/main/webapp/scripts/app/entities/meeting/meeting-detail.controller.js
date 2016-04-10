'use strict';

angular.module('lobbycalApp')
    .controller('MeetingDetailController', function ($scope, $stateParams, Meeting, Tag, Partner, User) {
        $scope.meeting = {};
        
        $scope.load = function (id) {
            Meeting.get({id: id}, function(result) {
              $scope.meeting = result;
              console.log(result);

            });
        };
        $scope.load($stateParams.id);
    });
