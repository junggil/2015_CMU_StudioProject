import threading, json
from daemon import client
from was.models import *
from pony.orm import db_session

VNODE_LIST = {}

@db_session
def init_vnode_list():
    for node in Node.select(lambda x:x.virtual):
        add_vnode(node.nodeId)

def get_vnode_profiles():
    return {
        'alarm': 'off',
        'door': 'close',
        'humidity': 23,
        'light': 'off',
        'proximity': 'vacant',
        'thermostat': 21,
    }

def add_vnode(nodeId):
    VNODE_LIST[nodeId] = get_vnode_profiles()

    def _send_heartbeat():
        if nodeId in VNODE_LIST:
            client.publish('/sanode/%s/heartbeat' % nodeId, '')
            threading.Timer(10, _send_heartbeat).start()

    def _response_qeury(client, userdata, message):
        client.publish('/sanode/%s/status' % nodeId, json.dumps(VNODE_LIST[nodeId]))

    def _response_control(client, userdata, message):
        global VNODE_LIST
        try:
            key, value = tuple(json.loads(str(message.payload, 'utf-8')).items())[0]
            VNODE_LIST[nodeId][key]['value'] = value
            client.publish('/sanode/'+nodeId+'/status', json.dumps({key: value}))
        except (KeyError, ValueError):
            pass

    client.message_callback_add('/sanode/%s/query' % nodeId, _response_qeury)
    client.message_callback_add('/sanode/%s/control' % nodeId, _response_control)
    _send_heartbeat()

@db_session
def del_vnode(nodeId):
    if VNODE_LIST.get(nodeId):
        global VNODE_LIST
        client.unsubscribe('/sanode/%s/query' % nodeId)
        client.unsubscribe('/sanode/%s/control' % nodeId)
        del VNODE_LIST[nodeId]
