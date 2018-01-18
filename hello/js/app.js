(function () {
'use strict';

angular.module('HelloApp', [])
.controller('HelloController', HelloController)
.service('HelloService', HelloService)
.constant('AuthorizationEndpoint', "https://gluuserver/oxauth/restv1/authorize")
.constant('TokenEndpoint', "https://gluuserver/oxauth/restv1/token")
.constant('RegistrationEndpoint', "https://gluuserver/oxauth/restv1/register")
.constant('ClientID', "@!57E7.F422.1BEC.8661!0001!2917.B555!0008!1057.2149.C76E.504A")
.constant('RedirectURI', "https://ariramba.tecgraf.puc-rio.br:8443/hello/callbackpage.html")
;

HelloController.$inject = ['HelloService'];

function HelloController(HelloService) {
  var ctrl = this; 

  ctrl.getlogin = function () {
    HelloService.loginGluu();
  }

  ctrl.postlogin = function () {
    HelloService.loginGluu2();
  }

}

HelloService.$inject = ['$http','AuthorizationEndpoint', 'ClientID', 'RedirectURI'];
function HelloService($http, AuthorizationEndpoint, ClientID, RedirectURI) {
  var service = this;

  service.loginGluu = function () {
    var response = $http({
      method: "GET",
      url: (AuthorizationEndpoint),
      params: {
        response_type: 'id_token token',
	      scope: 'openid profile email',
      	nonce: 'qualquertextoabc',
	      client_id: ClientID,
      	redirect_uri: RedirectURI,
        state: 'abc',
      	display: 'page',        
	      // prompt: 'login',
      }
    }).then(function(result){
      	console.log(result);
        return result.data;
    });
    return response;
  };


service.loginGluu2 = function () {
    
    var data = {
        response_type: 'id_token token',
      	client_id: ClientID,
      	scope: 'email',
      	redirect_uri: RedirectURI,
      	nonce: 'qualquertexto',
      	display: 'page',
      	prompt: 'login',
    };

    var response = $http.jsonp(AuthorizationEndpoint +"?callback=JSON_CALLBACK", data)
	    .success(function (data, status, headers, config){
     	 	console.log(data);
	        return data;
	    })
	    .error(function (data, status, headers, config){
     	 	console.log(data);
     	   return data;
	    });

	// var response = $http({
  //     method: "POST",
  //     url: (AuthorizationEndpoint),
  //     data: {
  //       response_type: 'id_token token',
  //     	client_id: ClientID,
  //     	scope: 'email',
  //     	redirect_uri: RedirectURI,
  //     	nonce: 'qualquertexto',
  //     	display: 'page',
  //     	prompt: 'login',
  //     },
  //     dataType: "json"
  //   }).then(function(result){
  //     	console.log(result);
  //       return result.data;
  //   });
    return response;
  };
 
}

})();
