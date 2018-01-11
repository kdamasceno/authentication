'''
Created on Jan 10, 2018

@author: lcm
'''
from csbase.util.proxy import CSBaseLoginProxy

import java
import csbase

class CSBaseTest(object):
    '''
    classdocs
    '''
    csbaseProxy = csbase.util.proxy.CSBaseLoginProxy


    def __init__(self):
        '''
        Constructor
        '''
        print "CSBase. Initialization"
        self.csbaseProxy = CSBaseLoginProxy("localhost","5099", True)
        #print "CSBase. Initialized successfully"
    def authenticate(self):
        logged_in = self.csbaseProxy.authenticate("lmachado", "leo")
        print logged_in
        logged_in = self.csbaseProxy.authenticate("admin", "1234555")
        print logged_in
