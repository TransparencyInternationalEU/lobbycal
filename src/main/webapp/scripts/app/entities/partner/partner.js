'use strict';

angular.module('lobbycalApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('partner', {
                parent: 'entity',
                url: '/partner',
                data: {
                    roles: ['ROLE_ADMIN'],
                    pageTitle: 'lobbycalApp.partner.home.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/partner/partners.html',
                        controller: 'PartnerController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('partner');
                        return $translate.refresh();
                    }]
                }
            })
            .state('partnerDetail', {
                parent: 'entity',
                url: '/partner/:id',
                data: {
                    roles: ['ROLE_ADMIN'],
                    pageTitle: 'lobbycalApp.partner.detail.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/partner/partner-detail.html',
                        controller: 'PartnerDetailController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('partner');
                        return $translate.refresh();
                    }]
                }
            });
    });
