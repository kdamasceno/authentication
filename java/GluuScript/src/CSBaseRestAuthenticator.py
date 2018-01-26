'''
Created on Jan 9, 2018

@author: lmachado
'''

from org.xdi.service.cdi.util import CdiUtil
from org.xdi.oxauth.security import Identity
from org.xdi.model.custom.script.type.auth import PersonAuthenticationType
from org.xdi.oxauth.service import UserService, AuthenticationService
from org.xdi.util import StringHelper
from org.xdi.oxauth.model.common import User

import httplib, urllib, json

class PersonAuthentication(PersonAuthenticationType):
    
    def __init__(self, currentTimeMillis):
        self.currentTimeMillis = currentTimeMillis

    def init(self, configurationAttributes):
        print "CSBaseRest. Initialization"
        return True

    def destroy(self, configurationAttributes):
        print "CSBaseRest. Destroy"
        print "CSBaseRest. Destroyed successfully"
        return True

    def getApiVersion(self):
        return 1

    def isValidAuthenticationMethod(self, usageType, configurationAttributes):
        return True

    def getAlternativeAuthenticationMethod(self, usageType, configurationAttributes):
        return None

    def authenticate(self, configurationAttributes, requestParameters, step):
        
        if (step == 1):
            print "CSBaseRest. Authenticate for step 1"
            
            identity = CdiUtil.bean(Identity)
            credentials = identity.getCredentials()

            user_name = credentials.getUsername()
            user_password = credentials.getPassword()

            authenticationService = CdiUtil.bean(AuthenticationService)
            userService = CdiUtil.bean(UserService)
            
            logged_in = False
            if (StringHelper.isNotEmptyString(user_name) and StringHelper.isNotEmptyString(user_password)):
                # Acessa o CSBASE para saber se login e senha estao ok
                responseData = self.getRestDataFor(configurationAttributes.get("CSBaseRestBaseURL").getValue2(), user_name, user_password)
                if (responseData.find("accessToken") > 0):
                    logged_in = True
                    responseObj = json.loads(responseData)
                    uid = responseObj["user"]["id"]
                    foundUser = userService.getUserByAttribute("uid", uid)
                    if (foundUser == None):
                        foundUser = self.createUserFor(responseObj, userService)
                    authenticationService.authenticate(foundUser.getUserId())
            if (not logged_in):
                return False

            return True
        else:
            return False
        
    def createUserFor(self, responseObj, userService):
        print "CSBaseRest: criando usuario para: " + responseObj["user"]["id"]
        fullName = responseObj["user"]["name"]
        firstName = fullName.split()[0]
        surName = fullName.partition(" ")[-1]
        uid = responseObj["user"]["id"]
        login = responseObj["user"]["login"]      
        newUser = User()
        newUser.setAttribute("sn", surName)
        newUser.setAttribute("uid", uid)
        newUser.setAttribute("cn", fullName)
        newUser.setAttribute("displayName", firstName)
        newUser.setAttribute("mail", login + "@tecgraf.puc-rio.br")
        newUser.setAttribute("profile", "CSBase")
        foundUser = userService.addUser(newUser, True)
        return foundUser
    
    def getRestDataFor(self, baseurl, login, password):
        data = ""
        print "CSBaseRest: acessando csbase na url base: "+baseurl
        params = urllib.urlencode({'login': login, 'password': password})
        headers = {"Content-type": "application/x-www-form-urlencoded"}
        conn = httplib.HTTPConnection(baseurl)
        conn.request("POST", "/v1/authentication", params, headers)
        response = conn.getresponse()
        data = response.read()
        conn.close()
        return data
    
    def prepareForStep(self, configurationAttributes, requestParameters, step):
        if (step == 1):
            print "CSBaseRest. Prepare for Step 1"
            return True
        else:
            return False

    def getExtraParametersForStep(self, configurationAttributes, step):
        return None

    def getCountAuthenticationSteps(self, configurationAttributes):
        return 1

    def getPageForStep(self, configurationAttributes, step):
        return ""

    def logout(self, configurationAttributes, requestParameters):
        return True
