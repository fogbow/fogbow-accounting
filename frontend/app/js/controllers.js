"use strict"

var fogbowAccountingControllers = angular.module('fogbowAccountingControllers', []);

fogbowAccountingControllers.controller('HomeCtrl', ['$scope', 'Usage', 'Nof', 
	function($scope, Usage, Nof) {
		$scope.initTabs = function() {
			$(".mytabs a").click(function(e){
				e.preventDefault();
				$(this).tab('show');
			});
		};
		$scope.initTabs();
		$scope.usage = Usage.query();
		$scope.members = Nof.query();
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

fogbowAccountingControllers.controller('UsageByUserCtrl', ['$scope', '$routeParams', 'UsageByUser', 
	function($scope, $routeParams, UsageByUser) {
		$scope.userId = $routeParams.userId;
		$scope.usage = UsageByUser.query({userId: $routeParams.userId});
	}]);

fogbowAccountingControllers.controller('MemberUsagePerUserCtrl', ['$scope', '$routeParams', 'Nof', 
	function($scope, $routeParams, Nof) {
		$scope.memberId = $routeParams.memberId;
		$scope.memberUsers = Nof.query({'memberId': $routeParams.memberId});
	}]);