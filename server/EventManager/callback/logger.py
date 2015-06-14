from callback import client
from pymongo import MongoClient
from datetime import datetime

db = MongoClient()['iot-anyware']

def insert_db(post, collection = 'sanode'):
    db[collection].insert_one(post)

def db_logging(client, userdata, message):
    node, msg_type = message.topic.split('/')[2:4]
    post = {'node': node, 'timestamp': datetime.now(), 'msg_type': msg_type, 'msg': str(message.payload, 'utf-8')}
    print(post)
    db['sanode'].insert_one(post)

client.message_callback_add('/sanode/+/status', db_logging)
client.message_callback_add('/sanode/+/control', db_logging)
