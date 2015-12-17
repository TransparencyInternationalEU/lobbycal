'use strict';

angular.module('lobbycalApp')
    .factory('Register', function ($resource) {
        return $resource('api/register', {}, {
        });
    });


