import json
from daemon import client
from was.models import *
from was.utils import smtp
from pony.orm import db_session

@db_session
def send_email_notification(client, userdata, message):
    try:
        node, msg_type = message.topic.split('/')[2:4]
        payload = json.loads(str(message.payload, 'utf-8'))

        if payload['type'] in ['alert']:
            for user in RegisteredNode.select(lambda x:x.node.nodeId == node):
                smtp.sendAlarm(user.user.email, user.nickName, json.loads(str(message.payload, 'utf-8'))['info'])
    except (ValueError, KeyError):
        pass

client.message_callback_add('/sanode/+/notify', send_email_notification)
