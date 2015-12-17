'use strict';

angular.module('lobbycalApp')
    .controller('LogoutController', function (Auth) {
        Auth.logout();
    });
