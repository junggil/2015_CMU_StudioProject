from was import app, db
from was.models import *
from was.views import session
from was.decorators import args, auth
from daemon import vnode
from flask import request, jsonify
from pony.orm import db_session, core, select
from pony.orm.core import ObjectNotFound

@app.route('/user/getNodeList', methods=['GET'])
@args.is_exists(body=['session'])
@auth.is_valid_client()
@auth.is_valid_session()
@db_session
def getNodeList():
    try:
        sessionOwner = Session[request.args.get('session')].sessionOwner
        res = []
        for node in select(node for node in RegisteredNode if node.user.email == sessionOwner):
            node_info = node.to_dict()
            del node_info['id']
            del node_info['user']
            del node_info['createDate']
            node_info['profiles'] = []
            for sensor in select(sensor for sensor in NodeProfile if sensor.node == node.node):
                node_info['profiles'].append({'name': sensor.name, 'profile': sensor.profile.name})
            res.append(node_info)
        return jsonify({'statusCode': 200, 'result': res})
    except ObjectNotFound:
        return jsonify({'statusCode': 400, 'result': 'Invalid session'})

@app.route('/user/registerNode', methods=['POST'])
@args.is_exists(body=['session', 'nodeId'])
@auth.is_valid_client()
@auth.is_valid_session()
@db_session
def registerNode():
    try:
        params = request.get_json()
        nodeId = params['nodeId']
        sessionOwner = Session[params['session']].sessionOwner

        if Node.get(nodeId=nodeId):
            return jsonify({'statusCode': 400, 'result': 'Already registed nodeId'})

        if params.get('virtual'):
            session.add_relation_node_to_user(nodeId, params.get('nickName', nodeId), sessionOwner, is_virtual=True)
            vnode.add_vnode(nodeId)
        else:
            old_pending_request = PendingRequestNode.get(nodeId = nodeId)
            if old_pending_request:
                if old_pending_request.user.email == sessionOwner:
                    old_pending_request.delete()
                    db.commit()
                else:
                    return jsonify({'statusCode': 400, 'result': 'Already registed nodeId'})

            PendingRequestNode(nodeId=nodeId, user=sessionOwner, nickName=params.get('nickName', nodeId))
            db.commit()
        return jsonify({'statusCode': 200, 'result': {}})
    except ObjectNotFound:
        return jsonify({'statusCode': 400, 'result': 'Invalid session'})

@app.route('/user/unregisterNode', methods=['POST'])
@args.is_exists(body=['session', 'nodeId'])
@auth.is_valid_client()
@auth.is_valid_session()
@db_session
def unregisterNode():
    try:
        params = request.get_json()
        sessionOwner = Session[params['session']].sessionOwner
        nodeId = params['nodeId']

        registerNode = RegisteredNode.get(user=sessionOwner, node=nodeId)
        if registerNode.owner:
            node = Node[nodeId]
            if node.virtual:
                vnode.del_vnode(nodeId)
            node.delete()
        else:
            registerNode.delete();
        db.commit()
        return jsonify({'statusCode': 200, 'result': {}})
    except ObjectNotFound:
        return jsonify({'statusCode': 400, 'result': 'Invalid nodeId'})

@app.route('/user/shareNode', methods=['POST'])
@args.is_exists(body=['session', 'nodeId', 'targetUser'])
@auth.is_valid_client()
@auth.is_valid_session()
@db_session
def shareNode():
    try:
        params = request.get_json()
        sessionOwner = Session[params['session']].sessionOwner
        nodeId = params['nodeId']
        nickName = RegisteredNode.get(owner=True, node=nodeId).nickName
        if RegisteredNode.get(user=params['targetUser'], node=nodeId):
            return jsonify({'statusCode': 400, 'result': 'Already registered node'})

        session.add_relation_node_to_user(nodeId, nickName, params['targetUser'], is_owner=False)
        db.commit()
        return jsonify({'statusCode': 200, 'result': {}})
    except ObjectNotFound:
        return jsonify({'statusCode': 400, 'result': 'Invalid nodeId'})

@app.route('/user/transferOwner', methods=['POST'])
@args.is_exists(body=['session', 'nodeId', 'targetUser'])
@auth.is_valid_client()
@auth.is_valid_session()
@db_session
def transferOwner():
    try:
        sessionOwner = Session[request.get_json()['session']].sessionOwner
        nodeId = request.get_json()['nodeId']
        prev_owner = RegisteredNode.get(owner=True, node=nodeId)

        if prev_owner.user.email != sessionOwner:
            return jsonify({'statusCode': 400, 'result': 'Permission denied'})
        prev_owner.owner = False
        RegisteredNode(owner = True, nickName = prev_owner.nickName, user = request.get_json()['targetUser'], node = nodeId)
        db.commit()
        return jsonify({'statusCode': 200, 'result': {}})
    except ObjectNotFound:
        return jsonify({'statusCode': 400, 'result': 'Invalid nodeId'})
    except ValueError:
        return jsonify({'statusCode': 400, 'result': 'Invalid targetUser'})

@app.route('/user/getConfiguration', methods=['GET'])
@args.is_exists(body=['session'])
@auth.is_valid_client()
@auth.is_valid_session()
@db_session
def getConfiguration():
    sessionOwner = Session[request.args.get('session')].sessionOwner
    config = UserConfiguration.get(user=sessionOwner).to_dict()
    del config['user']
    return jsonify({'statusCode': 200, 'result': config})

@app.route('/user/setConfiguration', methods=['PUT'])
@args.is_exists(body=['session', 'loggingHour'])
@auth.is_valid_client()
@auth.is_valid_session()
@db_session
def setConfiguration():
    try:
        sessionOwner = Session[request.get_json()['session']].sessionOwner
        config = UserConfiguration.get(user=sessionOwner)
        config.loggingHour = request.get_json()['loggingHour']
        db.commit()
        return jsonify({'statusCode': 200, 'result': {}})
    except ValueError:
        return jsonify({'statusCode': 400, 'result': 'Invalid loggingHour'})
