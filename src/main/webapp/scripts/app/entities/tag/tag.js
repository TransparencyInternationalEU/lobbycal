'use strict';

angular.module('lobbycalApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('tag', {
                parent: 'entity',
                url: '/tag',
                data: {
                    roles: ['ROLE_ADMIN'],
                    pageTitle: 'lobbycalApp.tag.home.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/tag/tags.html',
                        controller: 'TagController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('tag');
                        return $translate.refresh();
                    }]
                }
            })
            .state('tagDetail', {
                parent: 'entity',
                url: '/tag/:id',
                data: {
                    roles: ['ROLE_ADMIN'],
                    pageTitle: 'lobbycalApp.tag.detail.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/tag/tag-detail.html',
                        controller: 'TagDetailController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('tag');
                        return $translate.refresh();
                    }]
                }
            });
    });
