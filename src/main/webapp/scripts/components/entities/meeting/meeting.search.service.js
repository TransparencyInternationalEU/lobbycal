'use strict';

angular.module('lobbycalApp')
    .factory('MeetingSearch', function ($resource) {
        return $resource('api/_search/meetings/:query', {}, {
            'query': { method: 'GET', isArray: true}
        });
    });
