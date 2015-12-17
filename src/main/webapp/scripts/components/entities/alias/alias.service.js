'use strict';

angular.module('lobbycalApp')
    .factory('Alias', function ($resource, DateUtils) {
        return $resource('api/aliass/:id', {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    });
