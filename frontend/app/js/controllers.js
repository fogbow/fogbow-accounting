"use strict"

var fogbowAccountingControllers = angular.module('fogbowAccountingControllers', []);

fogbowAccountingControllers.controller('HomeCtrl', ['$scope', '$location', 'Usage', 'Nof', 
	function($scope, $location, Usage, Nof) {
		$scope.initTabs = function() {
			$(".mytabs a").click(function(e){
				e.preventDefault();
				$(this).tab('show');
			});
		};
		$scope.initTabs();
		$scope.usage = Usage.query();
		$scope.members = Nof.query();
		$scope.locationPath = $location.path();
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
		$scope.usage = UsageByUser.query({userId: $routeParams.userId, memberId: $routeParams.memberId});
	}]);

fogbowAccountingControllers.controller('ConsumedFromMemberPerUserCtrl', ['$scope', '$routeParams', 'LocalMemberConsumption', 
	function($scope, $routeParams, LocalMemberConsumption) {
		$scope.memberId = $routeParams.memberId;
		$scope.memberUsers = LocalMemberConsumption.query({'memberId': $routeParams.memberId});
	}]);

fogbowAccountingControllers.controller('DonatedToMemberPerUserCtrl', ['$scope', '$routeParams', 'LocalMemberDonation', 
	function($scope, $routeParams, LocalMemberDonation) {
		$scope.memberId = $routeParams.memberId;
		$scope.memberUsers = LocalMemberDonation.query({'memberId': $routeParams.memberId});
	}]);