'use strict';

angular.module('lobbycalApp')
    .factory('SubmitterSearch', function ($resource) {
        return $resource('api/_search/submitters/:query', {}, {
            'query': { method: 'GET', isArray: true}
        });
    });
