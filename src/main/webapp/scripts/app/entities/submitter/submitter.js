'use strict';

angular.module('lobbycalApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('submitter', {
                parent: 'entity',
                url: '/submitter',
                data: {
                    roles: ['ROLE_USER'],
                    pageTitle: 'lobbycalApp.submitter.home.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/submitter/submitters.html',
                        controller: 'SubmitterController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('submitter');
                        return $translate.refresh();
                    }]
                }
            })
            .state('submitterDetail', {
                parent: 'entity',
                url: '/submitter/:id',
                data: {
                    roles: ['ROLE_USER'],
                    pageTitle: 'lobbycalApp.submitter.detail.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/submitter/submitter-detail.html',
                        controller: 'SubmitterDetailController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('submitter');
                        return $translate.refresh();
                    }]
                }
            });
    });
