from utils import mongodb
from daemon import client
from datetime import datetime

def db_logging(client, userdata, message):
    node, msg_type = message.topic.split('/')[2:4]
    post = {'node': node, 'timestamp': datetime.now(), 'msg_type': msg_type, 'msg': str(message.payload, 'utf-8')}
    mongodb.insert_db(post)

client.message_callback_add('/sanode/+/status', db_logging)
client.message_callback_add('/sanode/+/control', db_logging)
