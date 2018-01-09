function createCORSRequest(method, url) {
  var xhr = new XMLHttpRequest();
  if ("withCredentials" in xhr) {

    // Check if the XMLHttpRequest object has a "withCredentials" property.
    // "withCredentials" only exists on XMLHTTPRequest2 objects.
    xhr.open(method, url, true);

  } else if (typeof XDomainRequest != "undefined") {

    // Otherwise, check if XDomainRequest.
    // XDomainRequest only exists in IE, and is IE's way of making CORS requests.
    xhr = new XDomainRequest();
    xhr.open(method, url);

  } else {
    // Otherwise, CORS is not supported by the browser.
    xhr = null;

  }
  return xhr;
}

function loadDoc(method) {
  var xhr = createCORSRequest('GET', 'https://gluuserver/oxauth/restv1/authorize');
  if (!xhr) {
     throw new Error('CORS not supported');
  }

  xhr.setRequestHeader("response_type", "id_token token");
  xhr.setRequestHeader("client_id", "@!57E7.F422.1BEC.8661!0001!2917.B555!0008!1057.2149.C76E.504A");
  xhr.setRequestHeader("scope", "email");
  xhr.setRequestHeader("redirect_uri", "https://ariramba.tecgraf.puc-rio.br:8443/hello/callbackpage.html");
  xhr.setRequestHeader("nonce", "qualquertexto");
  xhr.setRequestHeader("display", "page");
  xhr.setRequestHeader("prompt", "login");
  
  // Response handlers.
  xhr.onload = function() {
    var text = xhr.responseText;
    var title = getTitle(text);
    alert('Response from CORS request to ' + url + ': ' + title);
  };

  xhr.onerror = function() {
    alert('Woops, there was an error making the request.');
  };

  xhr.send();
}

// $.ajax({

//   // The 'type' property sets the HTTP method.
//   // A value of 'PUT' or 'DELETE' will trigger a preflight request.
//   type: 'GET',

//   // The URL to make the request to.
//   url: '"https://ariramba.tecgraf.puc-rio.br:8443/hello/callbackpage.html',

//   // The 'contentType' property sets the 'Content-Type' header.
//   // The JQuery default for this property is
//   // 'application/x-www-form-urlencoded; charset=UTF-8', which does not trigger
//   // a preflight. If you set this value to anything other than
//   // application/x-www-form-urlencoded, multipart/form-data, or text/plain,
//   // you will trigger a preflight request.
//   contentType: 'text/plain',

//   xhrFields: {
//     // The 'xhrFields' property sets additional fields on the XMLHttpRequest.
//     // This can be used to set the 'withCredentials' property.
//     // Set the value to 'true' if you'd like to pass cookies to the server.
//     // If this is enabled, your server must respond with the header
//     // 'Access-Control-Allow-Credentials: true'.
//     withCredentials: false
//   },

//   headers: {
//     // Set any custom headers here.
//     // If you set any non-simple headers, your server must include these
//     // headers in the 'Access-Control-Allow-Headers' response header.
//   },

//   success: function() {
//     // Here's where you handle a successful response.
//   },

//   error: function() {
//     // Here's where you handle an error response.
//     // Note that if the error was due to a CORS issue,
//     // this function will still fire, but there won't be any additional
//     // information about the error.
//   }
// });