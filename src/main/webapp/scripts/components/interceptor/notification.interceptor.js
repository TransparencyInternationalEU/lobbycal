 'use strict';

angular.module('lobbycalApp')
    .factory('notificationInterceptor', function ($q, AlertService) {
        return {
            response: function(response) {
                var alertKey = response.headers('X-lobbycalApp-alert');
                if (angular.isString(alertKey)) {
                    AlertService.success(alertKey, { param : response.headers('X-lobbycalApp-params')});
                }
                return response;
            }
        };
    });
