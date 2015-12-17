'use strict';

angular.module('lobbycalApp')
    .controller('AliasController', function ($scope, Alias, User, AliasSearch, ParseLinks) {
    	 
    	$scope.aliass = [];
        $scope.users = User.query();
        $scope.page = 1;
        $scope.loadAll = function() {
            Alias.query({page: $scope.page, per_page: 20}, function(result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.aliass = result;
            });
        };
        $scope.loadPage = function(page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();

        $scope.showUpdate = function (id) {
            Alias.get({id: id}, function(result) {
                $scope.alias = result;
                $('#saveAliasModal').modal('show');
            });
        };

        $scope.save = function () {
            if ($scope.alias.id != null) {
                Alias.update($scope.alias,
                    function () {
                        $scope.refresh();
                    });
            } else {
                Alias.save($scope.alias,
                    function () {
                        $scope.refresh();
                    });
            }
        };

        $scope.delete = function (id) {
            Alias.get({id: id}, function(result) {
                $scope.alias = result;
                $('#deleteAliasConfirmation').modal('show');
            });
        };

        $scope.confirmDelete = function (id) {
            Alias.delete({id: id},
                function () {
                    $scope.loadAll();
                    $('#deleteAliasConfirmation').modal('hide');
                    $scope.clear();
                });
        };

        $scope.search = function () {
            AliasSearch.query({query: $scope.searchQuery}, function(result) {
                $scope.aliass = result;
            }, function(response) {
                if(response.status === 404) {
                    $scope.loadAll();
                }
            });
        };

        $scope.refresh = function () {
            $scope.loadAll();
            $('#saveAliasModal').modal('hide');
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.alias = {alias: null, active: null, id: null};
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };
        
        $scope.randomize = function () {
            var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZ";
        	var string_length = 20;
        	var randomstring = '';
        	for (var i = 0; i < string_length; i++) {
        		var rnum = Math.floor(Math.random() * chars.length);
        		randomstring += chars.substring(rnum, rnum + 1);
        	}
        	document.getElementById("randomfield").value = randomstring;
        	$scope.alias.alias = randomstring;
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };
    });
