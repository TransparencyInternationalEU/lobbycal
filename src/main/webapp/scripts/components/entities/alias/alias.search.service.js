'use strict';

angular.module('lobbycalApp')
    .factory('AliasSearch', function ($resource) {
        return $resource('api/_search/aliass/:query', {}, {
            'query': { method: 'GET', isArray: true}
        });
    });
