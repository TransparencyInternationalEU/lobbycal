'use strict';

angular.module('lobbycalApp')
    .controller('SettingsController', function ($scope, Principal, User, Auth, Language, $translate) {
        $scope.success = null;
        $scope.error = null;
        Principal.identity(true).then(function(account) {
        	console.log(account);
        	 $scope.days = "3";
        	User.get({login: account.login}, function(result, headers) {
                $scope.days = headers('days');
                console.log(headers('days'));  
                console.log($scope.days);
            });
            $scope.settingsAccount = account;
            $scope.pluginUrl = "https://askyouradmin/api/meetings/dt/"+account.id;
          
        });

        $scope.save = function () {
            Auth.updateAccount($scope.settingsAccount).then(function() {
                $scope.error = null;
                $scope.success = 'OK';
                Principal.identity().then(function(account) {
                    $scope.settingsAccount = account;

                });
                Language.getCurrent().then(function(current) {
                    if ($scope.settingsAccount.langKey !== current) {
                        $translate.use($scope.settingsAccount.langKey);
                    }
                });
            }).catch(function() {
                $scope.success = null;
                $scope.error = 'ERROR';
            });
        };
    });
