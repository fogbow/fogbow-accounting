"use strict"

var fogbowAccountingControllers = angular.module('fogbowAccountingControllers', []);

fogbowAccountingControllers.controller('HomeCtrl', ['$scope', 'Usage', 
	function($scope, Usage) {
		$scope.initTabs = function() {
			$(".mytabs a").click(function(e){
				e.preventDefault();
				$(this).tab('show');
			});
		};
		$scope.initTabs();
		$scope.usage = Usage.query();
	}]);

fogbowAccountingControllers.controller('LoginCtrl', ['$scope', '$location', '$http', 'toastr',
	function($scope, $location, $http, toastr) {
		$scope.validateLogin = function () {
			if ($scope.authToken == undefined || $scope.authToken == '') {
				toastr.error('Please, fill the form with your authentication token!');
			} else {
				$http({
					method: 'POST',
					url: '/api/auth/login',
					data: $.param({'authToken': this.authToken}),
					headers: {"Content-type": "application/x-www-form-urlencoded"}
				}).then(function successCallback (response) {
					toastr.success('Great! You were connected to your account!');
					$location.path('/home');
				}, function errorCallback(response) {
					toastr.error('Invalid authentication token. Try again.');
				});
			}
		};
	}]);