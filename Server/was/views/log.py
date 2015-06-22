import json
from was import app
from was.models import *
from was.decorators import args, auth
from utils import mongodb
from flask import request, jsonify, json
from pony.orm import db_session, core, select
from pony.orm.core import ObjectNotFound
from datetime import datetime, timedelta

def search_db(userId):
    loggingHour = UserConfiguration[userId].loggingHour
    registeredNode = [reg.node.nodeId for reg in select(reg for reg in RegisteredNode \
                                      if reg.user.email == userId)]
    for post in mongodb.search_db({'timestamp': {'$gte': datetime.now() - timedelta(hours=loggingHour), '$lt': datetime.now()},
                                        'node': {'$in': registeredNode}}):
        try:
            val = dict(post)
            del val['_id']
            val['msg'] = json.loads(val['msg'])
            yield val
        except ValueError:
            pass

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
