'use strict';

angular.module('lobbycalApp')
    .controller('MeetingController', function ($scope, $window, Meeting, Tag, Partner, User, MeetingSearch, ParseLinks, Principal) {
    	Principal.identity(true).then(function(account) {
            console.log(account);

            $scope.curUser = account.id;
        });
    	$scope.meetings = [];
        $scope.tags = Tag.query({per_page: 999});
        $scope.partners = Partner.query({per_page: 999});
        $scope.users = User.query();
        $scope.page = 1;
        $scope.loadAll = function() {
            Meeting.query({page: $scope.page, per_page: 20}, function(result, headers) {
            	if(!Principal.isAuthenticated()){
        			console.log();
        			$window.open(headers('Location'), '_self')
        		}
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
            	console.log($scope.meeting);
                Meeting.update($scope.meeting,
                    function () {
                        $scope.refresh();
                    });
            } else {
            	console.log($scope.meeting);
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
            	console.log($scope.totalItems);
            	 $scope.totalItems =-1;
            	 console.log($scope.totalItems);
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
