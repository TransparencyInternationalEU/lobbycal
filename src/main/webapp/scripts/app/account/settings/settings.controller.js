'use strict';

angular.module('lobbycalApp')
    .controller('SettingsController', function ($scope, Principal, Auth, Language, $translate) {
        $scope.success = null;
        $scope.error = null;
        Principal.identity(true).then(function(account) {
            console.log(account.id);

            $scope.settingsAccount = account;
            $scope.pluginUrl = "https://lobbycal.greens-efa-service.eu/api/meetings/dt/"+account.id;
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
