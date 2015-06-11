from was import app, db, utils
from was.models import *
from was.decorators import args, auth
from uuid import uuid4
from flask import request, jsonify
from pony.orm import db_session, core, select
from datetime import datetime, timedelta

def _make_session(sessionOwner, sessionType):
    old_session = Session.get(lambda s:s.sessionOwner == sessionOwner)
    if old_session:
        old_session.delete()

    return Session(session = utils.model_unique_id(Session),
                   refreshToken = str(uuid4()), sessionType = sessionType, 
                   sessionOwner = sessionOwner)

def _hide_privates(session):
    session['expires'] = session['expires'].strftime('%s')
    for key in ['sessionType', 'sessionOwner', 'enabled', 'createDate']:
        del(session[key])
    return session

@app.route('/session/createUser', methods=['POST'])
@auth.is_valid_client()
@args.is_exists(body=['email', 'password'])
@db_session
def createUser():
    payload = request.get_json()
    if User.get(email=payload['email'], password=payload['password']):
        session = _make_session(payload['email'], 'user')
        db.commit()
        return jsonify({'statusCode': 200, 'result': _hide_privates(session.to_dict())})
    else:
        return jsonify({'statusCode': 400, 'result': 'Invalid user information'})

@app.route('/session/createNode', methods=['POST'])
@auth.is_valid_client()
@args.is_exists(body=['nodeId'])
@db_session
def createNode():
    pendingNode = PendingRequestNode.get(nodeId = request.get_json()['nodeId'])
    if not pendingNode or pendingNode.endDate < datetime.now():
        return jsonify({'statusCode': 400, 'result': 'Unregisterd nodeId'})
    else:
        Node(nodeId = pendingNode.nodeId)
        session = _make_session(pendingNode.nodeId, 'node')
        RegisteredNode(owner = True, nickName = pendingNode.nickName, user = pendingNode.user, node = pendingNode.nodeId)
        pendingNode.delete()
        db.commit()
        return jsonify({'statusCode': 200, 'result': _hide_privates(session.to_dict())})

@app.route('/session/refresh', methods=['POST'])
@auth.is_valid_client()
@args.is_exists(body=['session', 'refreshToken'])
@db_session
def refresh():
    payload = request.get_json()
    session = Session.get(session=payload['session'], refreshToken=payload['refreshToken'])
    if not session or not session.enabled:
        return jsonify({'statusCode': 400, 'result': 'Invalid session or refreshToken'})
    else:
        session.expires = datetime.now() + timedelta(hours=2)
        db.commit()
        return jsonify({'statusCode': 200, 'result': _hide_privates(session.to_dict())})