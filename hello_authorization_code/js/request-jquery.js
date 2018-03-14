// Endpoints do Gluu Server
var authorization_endpoint = 'https://gluuserver/oxauth/restv1/authorize';

// Endpoints da API REST
var loginEndpoint = 'https://ariramba:8456/webapi/login';
var logoutEndpoint = 'https://ariramba:8456/webapi/logout';
var demoEndpoint = 'https://ariramba:8456/webapi/demo';

// Configuração do cliente de exemplo previamente cadastrado
var CLIENTID = '@!57E7.F422.1BEC.8661!0001!2917.B555!0008!9081.2658.C5BD.4EF1';
var REDIRECT = 'https://ariramba.tecgraf.puc-rio.br:8443/hello_authorization_code';

function login() {
    // Parâmetros da Requisição de Autenticação OpenID
    var params = {
        scope: 'openid profile email',
        client_id: CLIENTID,
        // TODO Implementar uso de 'state'
        // state: 'teste1',
        // RECOMMENDED. Opaque value used to maintain state between the request and the callback. 
        // Typically, Cross-Site Request Forgery (CSRF, XSRF) mitigation is done by cryptographically
        // binding the value of this parameter with a browser cookie.
        redirect_uri: REDIRECT,
        response_type: 'code'
    };
    var _url = authorization_endpoint + '?' + jQuery.param(params); 
    var win = window.open(_url, "Login", 'width=800, height=700'); 
    var pollTimer = window.setInterval(function() { 
        try {
              if (win.document.URL.indexOf(REDIRECT) != -1) {
                  window.clearInterval(pollTimer);                  
                  var url =   win.document.URL;
                  code =   gup(url, 'code');                 
                  sendAuthRequest(code);
                  win.close();       
              }
        } catch(e) {
        
        }
    }, 100);
}

function sendAuthRequest(code) {    
    $.ajax({
        type: 'POST',
        url: loginEndpoint,
        data: {
            code: code,
            redirect_uri: REDIRECT
        },
        success: function(responseText){   
            tec_token = responseText.tec_token;   
            sessionStorage.setItem("tec_token", tec_token);            
            applyLogin(tec_token);
        }
    });
}

function applyLogin(tec_token){
    var decoded_token = jwt_decode(tec_token);
    showUserInfo(decoded_token.user);
    $('#loginText').hide();
    $('#logoutText').show();
    loggedIn = true;
}

function showUserInfo(user) {    
    debugger;
   $('#uName').text('Bem-vindo(a), ' + user.given_name + '!');
   $('#uPicture').attr('src', user.picture);
   $('#uEmail').text(user.email);
   $('#uEmail').attr('href', user.email); 
   $('#data').text('Pode consultar dados privados');
}

function logout(){
    // TODO criar endpoint da URL no servidor e cadastrar no cliente gluu
  $('#myIFrame').location = logoutEndpoint; 
  sessionStorage.clear();
  startLogoutPolling();
  return false;
}

function startLogoutPolling() {
    $('#loginText').show();    
    $('#logoutText').hide();
    loggedIn = false;
    $('#uName').text(' ');
    $('#uPicture').attr('src', '');
    $('#uEmail').text(' ');
    $('#uEmail').attr('href','#');
    $('#data').text(' ');
}

//credits: http://www.netlobo.com/url_query_string_javascript.html
function gup(url, name) {
   name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
   var regexS = "[\\#&?]"+name+"=([^&#]*)";
   var regex = new RegExp( regexS );
   var results = regex.exec( url );
   if( results == null )
      return "";
   else
      return results[1];
}

function getData(){
    var tec_token = sessionStorage.getItem("tec_token");
    if (!tec_token){
        $('#data').text('Usuario precisa fazer login');
    }else{
        $.ajax({
            type: 'GET',        
            url: demoEndpoint,
            headers: {
                "Authorization" : "Bearer " + tec_token
            },
            success: function(resp) {
                data    =   resp;
                $('#data').text(data.msg);            
            },
            error: function(resp) {
                $('#data').text("Sem acesso");
            }
        });   
    }
}