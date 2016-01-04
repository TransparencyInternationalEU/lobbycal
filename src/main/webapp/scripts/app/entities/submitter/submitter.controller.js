'use strict';

angular.module('lobbycalApp')
    .controller('SubmitterController', function ($scope, Submitter, User, SubmitterSearch, ParseLinks) {
        $scope.submitters = [];
        $scope.users = User.query();
        $scope.page = 1;
        $scope.loadAll = function() {
            Submitter.query({page: $scope.page, per_page: 20}, function(result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.totalItems = headers('X-Total-Count');
                $scope.submitters = result;
            });
        };
        $scope.loadPage = function(page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();

        $scope.showUpdate = function (id) {
            Submitter.get({id: id}, function(result) {
                $scope.submitter = result;
                $('#saveSubmitterModal').modal('show');
            });
        };

        $scope.save = function () {
            if ($scope.submitter.id != null) {
                Submitter.update($scope.submitter,
                    function () {
                        $scope.refresh();
                    });
            } else {
                Submitter.save($scope.submitter,
                    function () {
                        $scope.refresh();
                    });
            }
        };

        $scope.delete = function (id) {
            Submitter.get({id: id}, function(result) {
                $scope.submitter = result;
                $('#deleteSubmitterConfirmation').modal('show');
            });
        };

        $scope.confirmDelete = function (id) {
            Submitter.delete({id: id},
                function () {
                    $scope.loadAll();
                    $('#deleteSubmitterConfirmation').modal('hide');
                    $scope.clear();
                });
        };

        $scope.search = function () {
            SubmitterSearch.query({query: $scope.searchQuery}, function(result) {
                $scope.submitters = result;
            }, function(response) {
                if(response.status === 404) {
                    $scope.loadAll();
                }
            });
        };

        $scope.refresh = function () {
            $scope.loadAll();
            $('#saveSubmitterModal').modal('hide');
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.submitter = {email: null, active: null, version: null, id: null};
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };
    });
