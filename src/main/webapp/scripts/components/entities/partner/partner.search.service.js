'use strict';

angular.module('lobbycalApp')
    .factory('PartnerSearch', function ($resource) {
        return $resource('api/_search/partners/:query', {}, {
            'query': { method: 'GET', isArray: true}
        });
    });
