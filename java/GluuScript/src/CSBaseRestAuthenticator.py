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
            
            print "CSBaseRest: username: "+user_name+" - passwd: "+user_password
            logged_in = False
            if (StringHelper.isNotEmptyString(user_name) and StringHelper.isNotEmptyString(user_password)):
                #responseData = self.getRestDataFor(configurationAttributes.get("CSBaseRestBaseURL").getValue2(), user_name, user_password)
                # Acessa o CSBASE para saber se login e senha estao ok
                baseurl = configurationAttributes.get("CSBaseRestBaseURL").getValue2()
                print "CSBaseRest: acessando csbase na url base: "+baseurl
                params = urllib.urlencode({'login': user_name, 'password': user_password})
                headers = {"Content-type": "application/x-www-form-urlencoded"}
                print "CSBaseRest: conectando..."
                conn = httplib.HTTPConnection(baseurl)
                print "CSBaseRest: fazendo requisicao..."
                conn.request("POST", "/v1/authentication", params, headers)
                print "CSBaseRest: obtendo reposta..."
                response = conn.getresponse()
                print "CSBaseRest: status da resposta do csbase: "+ str(response.status)
                responseData = response.read()
                print "CSBaseRest: tamanho da resposta: " + str(len(responseData))
                conn.close()
                print "CSBaseRest: fechando a conexao"
                if (responseData.find("accessToken") > 0):
                    logged_in = True
                    responseObj = json.loads(responseData)
                    uid = responseObj["user"]["id"]
                    foundUser = userService.getUserByAttribute("uid", uid)
                    if (foundUser == None):
                        print "CSBaseRest: criando usuario para: " + uid
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
                    authServiceRet = authenticationService.authenticate(foundUser.getUserId())
                    print "CSBaseRest: retorno do authService: "+str(authServiceRet)
            print "CSBaseRest: terminou"
            if (not logged_in):
                return False

            return True
        else:
            return False
        
    '''
    def getRestDataFor(self, baseurl, login, password):
        print "CSBaseRest: acessando csbase na url base: "+baseurl
        params = urllib.urlencode({'login': login, 'password': password})
        headers = {"Content-type": "application/x-www-form-urlencoded"}
        print "CSBaseRest: conectando..."
        conn = httplib.HTTPConnection(baseurl)
        print "CSBaseRest: fazendo requisicao..."
        conn.request("POST", "/v1/authentication", params, headers)
        print "CSBaseRest: obtendo reposta..."
        response = conn.getresponse()
        print "CSBaseRest: status da resposta do csbase: "+ str(response.status)
        data = response.read()
        conn.close()
        return data
    '''
    
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
