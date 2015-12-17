'use strict';

angular.module('lobbycalApp')
    .factory('Submitter', function ($resource, DateUtils) {
        return $resource('api/submitters/:id', {}, {
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
