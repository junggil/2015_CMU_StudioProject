import json
from was import app
from was.models import *
from was.decorators import args, auth
from flask import request, jsonify, json
from pony.orm import db_session, core, select
from pony.orm.core import ObjectNotFound
from pymongo import MongoClient
from datetime import datetime, timedelta

def search_db(userId, collection = 'sanode'):
    db = MongoClient()['iot-anyware']
    loggingHour = UserConfiguration[userId].loggingHour
    registeredNode = [reg.node.nodeId for reg in select(reg for reg in RegisteredNode \
                                      if reg.user.email == 'simpson.homer@gmail.com')]
    for post in db[collection].find({'timestamp': {'$gte': datetime.now() - timedelta(hours=loggingHour), '$lt': datetime.now()},
                                     'node': {'$in': registeredNode}}):
        val = dict(post)
        del val['_id']
        val['msg'] = json.loads(val['msg'])
        yield val

@app.route('/log/getHistory', methods=['GET'])
@args.is_exists(body=['session'])
@auth.is_valid_client()
@auth.is_valid_session()
@db_session
def getHistory():
    try:
        sessionOwner = Session[request.args.get('session')].sessionOwner
        return jsonify({'statusCode': 200, 'result': list(search_db(sessionOwner))[::-1]})
    except ObjectNotFound:
        return jsonify({'statusCode': 400, 'result': 'Invalid profile name'})
