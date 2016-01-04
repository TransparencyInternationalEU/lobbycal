'use strict';

angular.module('lobbycalApp')
    .controller('TagController', function ($scope, Tag, Meeting, TagSearch, ParseLinks) {
        $scope.tags = [];
        $scope.meetings = Meeting.query();
        $scope.page = 1;
        $scope.loadAll = function() {
            Tag.query({page: $scope.page, per_page: 20}, function(result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.totalItems = headers('X-Total-Count');
                $scope.tags = result;
            });
        };
        $scope.loadPage = function(page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();

        $scope.showUpdate = function (id) {
            Tag.get({id: id}, function(result) {
                $scope.tag = result;
                $('#saveTagModal').modal('show');
            });
        };

        $scope.save = function () {
            if ($scope.tag.id != null) {
                Tag.update($scope.tag,
                    function () {
                        $scope.refresh();
                    });
            } else {
                Tag.save($scope.tag,
                    function () {
                        $scope.refresh();
                    });
            }
        };

        $scope.delete = function (id) {
            Tag.get({id: id}, function(result) {
                $scope.tag = result;
                $('#deleteTagConfirmation').modal('show');
            });
        };

        $scope.confirmDelete = function (id) {
            Tag.delete({id: id},
                function () {
                    $scope.loadAll();
                    $('#deleteTagConfirmation').modal('hide');
                    $scope.clear();
                });
        };

        $scope.search = function () {
            TagSearch.query({query: $scope.searchQuery}, function(result) {
                $scope.tags = result;
            }, function(response) {
                if(response.status === 404) {
                    $scope.loadAll();
                }
            });
        };

        $scope.refresh = function () {
            $scope.loadAll();
            $('#saveTagModal').modal('hide');
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.tag = {i18nKey: null, de: null, en: null, fr: null, es: null, id: null};
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };
    });
