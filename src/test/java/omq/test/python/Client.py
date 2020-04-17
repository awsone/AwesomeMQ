'''
Created on 03/07/2013

@author: Sergi Toda <sergi.toda@estudiants.urv.cat>
'''
import pika
import uuid
import json

class Client(object):
    '''
    classdocs
    '''
    

    def __init__(self, env):
        '''
        Constructor
        '''
        self.rpc_exchange = env.get('exchange', 'rpc_exchange')
        self.reply_queue = env.get('reply_queue', 'reply_queue') 
        
        self.host = env.get('host', 'localhost')
        self.port = env.get('port', 5672)   
        self.ssl = env.get('ssl', False)
        
        self.credentials = pika.PlainCredentials(env.get('user', 'guest'), env.get('pass', 'guest'))
        
        self.connection = pika.BlockingConnection(pika.ConnectionParameters(host=self.host, port=self.port, credentials=self.credentials, ssl=self.ssl))
         
        self.channel = self.connection.channel()
        
        self.callback_queue = self.channel.queue_declare(queue=self.reply_queue)
        
        self.channel.basic_consume(self.on_response, no_ack=True, queue=self.reply_queue)
        
    def async_call(self, uid, method, params):
        """ Async call this function will invoke the method 'method' with the params 'params' in the object binded with 'uid'"""
        self.__async(uid, method, params, False)
        
    def multi_async_call(self, uid, method, params):
        self.__async(uid, method, params, True)
    
    def __async(self, uid, method, params, multi):
        if multi:
            exch = "multi#" + uid
        else:
            exch = self.rpc_exchange
        
        corr_id = str(uuid.uuid4())
        request = json.dumps({"method": method, "params": params, "id": corr_id, "async":"true"})
        
        props = pika.BasicProperties(app_id=uid, correlation_id=corr_id, reply_to="", type='gson')
        
        self.channel.basic_publish(exchange=exch, routing_key=uid, properties=props, body=request)
        
        print "UID: " + uid + ", exchange: " + exch + " ,corrId = " + corr_id + " , json: " + request
            
    def sync_call(self, uid, method, params):
        self.response = None
        self.corr_id = str(uuid.uuid4())
        request = json.dumps({"method": method, "params": params, "id": self.corr_id, "async":"false"})
        
        props = pika.BasicProperties(app_id=uid, correlation_id=self.corr_id, reply_to="reply_queue", type='gson')
        
        self.channel.basic_publish(exchange=self.rpc_exchange, routing_key=uid, properties=props, body=request)
        
        print "UID: " + uid + ", exchange: " + self.rpc_exchange + " ,corrId = " + self.corr_id + " , json: " + request
        
        return self.__get_response()
    
    def multi_sync_call(self, uid, method, params, wait):
        responses = []
        self.response = None
        self.corr_id = str(uuid.uuid4())
        rpc_exchange = "multi#" + uid
        request = json.dumps({"method": method, "params": params, "id": self.corr_id, "async":"false"})       
        
        props = pika.BasicProperties(app_id=uid, correlation_id=self.corr_id, reply_to="reply_queue", type='gson')
        
        self.channel.basic_publish(exchange=rpc_exchange, routing_key=uid, properties=props, body=request)
        
        print "UID: " + uid + ", exchange: " + rpc_exchange + " ,corrId = " + self.corr_id + " , json: " + request
        i = 0
        while i < wait:
            responses.append(self.__get_response())
            self.response = None
            i = i + 1
        return responses        
    
    def __get_response(self):
        while  self.response is None:
            self.connection.process_data_events()
        return self.response
    
    def on_response(self, ch, method, props, body):
        if self.corr_id == props.correlation_id:
            result = json.loads(body)
            self.response = result["result"]
        
        
