'''
Created on 04/07/2013

@author: Sergi Toda <sergi.toda@estudiants.urv.cat>
'''
import unittest
from Client import Client
import time

class Test(unittest.TestCase):
    env = {'user': 'guest', 'pass':'guest', 'host' : 'localhost', 'port': 5672, 'exchange': 'rpc_exchange'}
    
    client = Client(env)

    def testAsync(self):
        self.client.async_call("calculator", "fibonacci", [10])
    
    def testSyncAdd(self):
        x = 10
        y = 5
        expected = x + y
        actual = self.client.sync_call("calculator", "add", [x, y])
        self.assertEqual(expected, actual)
    
    def testMultiSyncAdd(self):
        x = 20
        y = 10
        expected = x + y
        actual = self.client.multi_sync_call("calculator", "add", [x, y], 2)
        self.assertEqual(expected, actual[0])
        self.assertEqual(expected, actual[1])
        
    def testPi(self):
        expected = 3.1415
        actual = self.client.sync_call("calculator", "getPi", [])
        self.assertEqual(expected, actual)
    
    def testContact(self):
        address = {"street":"calle falsa", "number":2, "town":"Tgn"}
        emails = ["asdf@gmail.com", "asdf@hotmail.com"]
    
        name1 = "Superman"
        contact = {"name":name1, "surname":"T", "phone":"1234", "age":21, "address": address, "emails":emails}
        self.client.async_call("list", "setContact", [contact])
        time.sleep(1)
    
        name2 = "Batman"
        contact = {"name":name2, "surname":"S", "phone":"1234", "age":22, "address": address, "emails":emails}
        self.client.async_call("list", "setContact", [contact])
        time.sleep(1)
    
        clist = self.client.sync_call("list", "getContacts", [])
        c1 = clist[0]
        c2 = clist[1]
        
        self.assertEqual(name1, c1['name'])
        self.assertEqual(name2, c2['name'])
    
if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
