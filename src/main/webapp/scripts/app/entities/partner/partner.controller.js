'use strict';

angular.module('lobbycalApp')
    .controller('PartnerController', function ($scope, Partner, Meeting, PartnerSearch, ParseLinks) {
        $scope.partners = [];
        $scope.meetings = Meeting.query();
        $scope.page = 1;
        $scope.loadAll = function() {
            Partner.query({page: $scope.page, per_page: 20}, function(result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.totalItems = headers('X-Total-Count');
                $scope.partners = result;
            });
        };
        $scope.loadPage = function(page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();

        $scope.showUpdate = function (id) {
            Partner.get({id: id}, function(result) {
                $scope.partner = result;
                $('#savePartnerModal').modal('show');
            });
        };

        $scope.save = function () {
            if ($scope.partner.id != null) {
                Partner.update($scope.partner,
                    function () {
                        $scope.refresh();
                    });
            } else {
                Partner.save($scope.partner,
                    function () {
                        $scope.refresh();
                    });
            }
        };

        $scope.delete = function (id) {
            Partner.get({id: id}, function(result) {
                $scope.partner = result;
                $('#deletePartnerConfirmation').modal('show');
            });
        };

        $scope.confirmDelete = function (id) {
            Partner.delete({id: id},
                function () {
                    $scope.loadAll();
                    $('#deletePartnerConfirmation').modal('hide');
                    $scope.clear();
                });
        };

        $scope.search = function () {
            PartnerSearch.query({query: $scope.searchQuery}, function(result) {
                $scope.partners = result;
            }, function(response) {
                if(response.status === 404) {
                    $scope.loadAll();
                }
            });
        };

        $scope.refresh = function () {
            $scope.loadAll();
            $('#savePartnerModal').modal('hide');
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.partner = {name: null, transparencyRegisterID: null, id: null};
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };
    });
