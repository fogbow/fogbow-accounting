"use strict"

/* App Module */

var fogbowAccountingApp = angular.module('fogbowAccountingApp', [
	'ngRoute',
	'ngAnimate',
	'toastr',
	'fogbowAccountingControllers',
	'fogbowAccountingServices'
]);

fogbowAccountingApp.config(['$routeProvider', 
	function($routeProvider){
		$routeProvider.
			when('/home', {
				templateUrl: 'templates/home.phtml',
				controller: 'HomeCtrl'
			}).when('/login', {
				templateUrl: 'templates/login.phtml',
				controller: 'LoginCtrl'
			}).when('/usage/member/:memberId', {
				templateUrl: 'templates/consumptionperuser.phtml',
				controller: 'MemberUsagePerUserCtrl'
			}).when('/usage/member/:memberId/user/:userId', {
				templateUrl: 'templates/user.phtml',
				controller: 'UsageByUserCtrl'
			}).otherwise({
				redirectTo: '/home'
			});
	}]).run(function($rootScope, $location, $http, toastr) {
		$rootScope.$on('$routeChangeStart', function(event, current, next) {
			if ($location.path() != "/login") {
				$http({
					method: "GET",
					url: '/api/auth/checkSession'
				}).then(function successCallback(response){
					/* Do nothing. The user is authenticated */
					$("#loginmenu").hide();
					$("#logoutmenu").show();
				}, function errorCallback(response) {
					toastr.warning('Please, log into your account to see this page', 'Unauthorized');
					$("#loginmenu").show();
					$("#logoutmenu").hide();
					$location.path('/login');
				});
			}
		});
		$rootScope._logout = function logout(event) {
			event.preventDefault();
			$http({
				method: "GET",
				url: '/api/auth/logout'
			}).then(function successCallback(response){
				toastr.info('You are now disconnected');
				$("#loginmenu").show();
				$("#logoutmenu").hide();
				$location.path('/login');
			}, function errorCallback(response) {
				toastr.error('Problem while disconnecting. Try again.');
			});
		}
		$rootScope._localMember = "servers.lsd.ufcg.edu.br";
	});