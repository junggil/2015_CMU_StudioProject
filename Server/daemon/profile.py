import json
from daemon import client
from was.models import *
from pony.orm import db_session

@db_session
def update_node_profile(client, userdata, message):
    node, msg_type = message.topic.split('/')[2:4]
    for key, val in json.loads(str(message.payload, 'utf-8')).items():
        if not NodeProfile.get(node):
            NodeProfile(node=node, name=key, profile=val)
    db.commit()

client.message_callback_add('/sanode/+/profile', update_node_profile)
