'use strict';

angular.module('lobbycalApp')
    .controller('MeetingController', function ($scope, Meeting, Tag, Partner, User, MeetingSearch, ParseLinks) {
        $scope.meetings = [];
        $scope.tags = Tag.query({per_page: 999});
        $scope.partners = Partner.query({per_page: 999});
        $scope.users = User.query();
        $scope.page = 1;
        $scope.loadAll = function() {
            Meeting.query({page: $scope.page, per_page: 20}, function(result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.totalItems = headers('X-Total-Count');
                $scope.meetings = result;
            });
        };
        $scope.loadPage = function(page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();
        

        $scope.showUpdate = function (id) {
            Meeting.get({id: id}, function(result) {
                $scope.meeting = result;
                $('#saveMeetingModal').modal('show');
            });
        };

        $scope.save = function () {
            if ($scope.meeting.id != null) {
                Meeting.update($scope.meeting,
                    function () {
                        $scope.refresh();
                    });
            } else {
                Meeting.save($scope.meeting,
                    function () {
                        $scope.refresh();
                    });
            }
        };

        $scope.delete = function (id) {
            Meeting.get({id: id}, function(result) {
                $scope.meeting = result;
                $('#deleteMeetingConfirmation').modal('show');
            });
        };

        $scope.confirmDelete = function (id) {
            Meeting.delete({id: id},
                function () {
                    $scope.loadAll();
                    $('#deleteMeetingConfirmation').modal('hide');
                    $scope.clear();
                });
        };

        $scope.search = function () {
            MeetingSearch.query({query: $scope.searchQuery}, function(result) {
            	console.log(result);
                $scope.meetings = result;
            }, function(response) {
                if(response.status === 404) {
                    $scope.loadAll();
                }
            });
        };

        $scope.refresh = function () {
            $scope.loadAll();
            $('#saveMeetingModal').modal('hide');
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.meeting = {title: null, mTag: null, mPartner: null, submitter: null, aliasUsed: null, startDate: null, endDate: null, uid: null, id: null};
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };
        
        $scope.datePickerForDate = {};

        $scope.datePickerForDate.status = {
            opened: false
        };

        $scope.datePickerForDateOpen = function($event) {
            $scope.datePickerForDate.status.opened = true;
        };
        
        
        
        $scope.datePickerForEndDate = {};

        $scope.datePickerForEndDate.status = {
            opened: false
        };

        $scope.datePickerForEndDateOpen = function($event) {
            $scope.datePickerForEndDate.status.opened = true;
        };
    });
