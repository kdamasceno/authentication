'''
Created on Jan 9, 2018

@author: lmachado
'''

from org.xdi.service.cdi.util import CdiUtil
from org.xdi.oxauth.security import Identity
from org.xdi.model.custom.script.type.auth import PersonAuthenticationType
from org.xdi.util import StringHelper
from csbase.util.proxy import CSBaseLoginProxy

import java
import csbase

class PersonAuthentication(PersonAuthenticationType):
    csbaseProxy = csbase.util.proxy.CSBaseLoginProxy
    
    def __init__(self, currentTimeMillis):
        self.currentTimeMillis = currentTimeMillis

    def init(self, configurationAttributes):
        print "CSBase. Initialization"
        self.csbaseProxy = CSBaseLoginProxy(configurationAttributes.get("CSBaseServer").getValue2(),configurationAttributes.get("CSBasePort").getValue2(), True)
        return True

    def destroy(self, configurationAttributes):
        print "CSBase. Destroy"
        print "CSBase. Destroyed successfully"
        return True

    def getApiVersion(self):
        return 1

    def isValidAuthenticationMethod(self, usageType, configurationAttributes):
        return True

    def getAlternativeAuthenticationMethod(self, usageType, configurationAttributes):
        return None

    def authenticate(self, configurationAttributes, requestParameters, step):
        
        if (step == 1):
            print "CSBase. Authenticate for step 1"
            identity = CdiUtil.bean(Identity)
            credentials = identity.getCredentials()

            user_name = credentials.getUsername()
            user_password = credentials.getPassword()

            logged_in = False
            if (StringHelper.isNotEmptyString(user_name) and StringHelper.isNotEmptyString(user_password)):
                logged_in = self.csbaseProxy.authenticate(user_name, user_password)

            if (not logged_in):
                return False

            return True
        else:
            return False

    def prepareForStep(self, configurationAttributes, requestParameters, step):
        if (step == 1):
            print "CSBase. Prepare for Step 1"
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