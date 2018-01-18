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
    alert('CORS is not supported by the browser');
  }
  return xhr;
}

function sendRequest(method, url, redirect_uri, client_id) {
  var params = {
    response_type: 'id_token token',
    client_id: client_id,
    scope: 'openid profile email',
    redirect_uri: redirect_uri,
    nonce: 'qualquertexto3',
    display: 'page',
    state: 'testestate3'
  };
  url += '?' + jQuery.param( params ); 

  var xhr = createCORSRequest(method, url);
  if (!xhr) {
     throw new Error('CORS not supported');
  }

  // console.log(url);
  // debugger;
  
  // Response handlers.
  xhr.onload = function() {
    var text = xhr.responseText;
    alert('Response from CORS request to ' + url + ' => '+text);
    console.log('responseText = ' + text);
  };

  xhr.onerror = function() {
    alert('Woops, there was an error making the request.');
  };

  // xhr.onreadystatechange = function(){
  //   console.log(xhr);
  // }

  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4) { 
      if (xhr.status == 200) { 
        try {
         location = xhr.getResponseHeader("Location");
         console.log("Location is: " + location);
        }
        catch(e){
          console.log("Error reading the response: " + e.toString());
        }
      }
    }
  }

  xhr.send();
}