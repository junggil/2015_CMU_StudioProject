import threading, json
from was.models import *
from daemon import client
from pony.orm import db_session
from datetime import datetime, timedelta
from pony.orm.core import ObjectNotFound

def _publish_node_status_change(node, is_disconnected = False):
    for user in RegisteredNode.select(lambda x:x.node == node):
        client.publish('/user/%s/heartbeat' % user.user.email, \
               json.dumps({'node': user.node.nodeId, 'nickName': user.nickName, 'status': 'off' if is_disconnected else 'on'}))

@db_session
def update_last_heartbeat(client, userdata, message):
    try:
        nodeId, msg_type = message.topic.split('/')[2:4]
        node = Node[nodeId] 
        if not node.lastHeartBeat:
            _publish_node_status_change(node, is_disconnected = False)
        node.lastHeartBeat = datetime.now()
        db.commit()
    except ObjectNotFound:
        pass

@db_session
def check_expired_heartbeat():
    for node in Node.select():
        if node.lastHeartBeat and node.lastHeartBeat < datetime.now() - timedelta(seconds=30):
            node.lastHeartBeat = None
            _publish_node_status_change(node, is_disconnected = True)
            db.commit()
    threading.Timer(10, check_expired_heartbeat).start()

check_expired_heartbeat()
client.message_callback_add('/sanode/+/heartbeat', update_last_heartbeat)
