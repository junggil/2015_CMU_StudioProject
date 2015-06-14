import json
from was import app
from was.models import Profile
from was.decorators import args, auth
from flask import request, jsonify
from pony.orm import db_session
from pony.orm.core import ObjectNotFound

@app.route('/profile/getDetail', methods=['GET'])
@args.is_exists(body=['session', 'profile'])
@auth.is_valid_client()
@auth.is_valid_session()
@db_session
def getDetail():
    try:
        profile = Profile[request.args.get('profile')]
        return jsonify({'statusCode': 200, 'result': json.loads(profile.data)})
    except ObjectNotFound:
        return jsonify({'statusCode': 400, 'result': 'Invalid profile name'})
