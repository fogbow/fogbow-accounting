'use strict'

/* Fogbow Accounting Services */

var fogbowAccountingServices = angular.module('fogbowAccountingServices', ['ngResource']);

fogbowAccountingServices.factory('Usage', ['$resource', 
	function($resource) {
		return $resource('/api/usage/:memberId', {}, {
			query: {method:'GET', params:{'memberId': ''}, isArray: true}
		});
	}]);

fogbowAccountingServices.factory('UsageByUser', ['$resource', 
	function($resource) {
		return $resource('/api/usage/member/:memberId/user/:userId');
	}]);

fogbowAccountingServices.factory('Nof', ['$resource', 
	function($resource) {
		return $resource('/api/usage/members/:memberId', {'memberId': ''});
	}]);