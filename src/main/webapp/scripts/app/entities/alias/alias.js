'use strict';


angular
		.module('lobbycalApp')
		.config(
				function($stateProvider) {
					$stateProvider
							.state(
									'alias',
									{
										parent : 'entity',
										url : '/alias',
										data : {
											roles : [ 'ROLE_ANONYMOUS',
													'ROLE_USER' ],
											pageTitle : 'lobbycalApp.alias.home.title'
										},
										views : {
											'content@' : {
												templateUrl : 'scripts/app/entities/alias/aliass.html',
												controller : 'AliasController'
											}
										},
										resolve : {
											translatePartialLoader : [
													'$translate',
													'$translatePartialLoader',
													function($translate,
															$translatePartialLoader) {
														$translatePartialLoader
																.addPart('alias');
														return $translate
																.refresh();
													} ]
										}
									})
							.state(
									'aliasDetail',
									{
										parent : 'entity',
										url : '/alias/:id',
										data : {
											roles : [ 'ROLE_USER', 'ROLE_ADMIN' ],
											pageTitle : 'lobbycalApp.alias.detail.title'
										},
										views : {
											'content@' : {
												templateUrl : 'scripts/app/entities/alias/alias-detail.html',
												controller : 'AliasDetailController'
											}
										},
										resolve : {
											translatePartialLoader : [
													'$translate',
													'$translatePartialLoader',
													function($translate,
															$translatePartialLoader) {
														$translatePartialLoader
																.addPart('alias');
														return $translate
																.refresh();
													} ]
										}
									});
				});
