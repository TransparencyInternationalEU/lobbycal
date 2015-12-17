'use strict';

angular.module('lobbycalApp')
    .controller('LanguageController', function ($scope, $translate, Language, tmhDynamicLocale) {
        $scope.changeLanguage = function (languageKey) {
            $translate.use(languageKey);
            tmhDynamicLocale.set(languageKey);
        };

        Language.getAll().then(function (languages) {
            $scope.languages = languages;
        });
    });
