from was import app, db
from was.models import *
from was.decorators import args, auth
from flask import request, jsonify
from pony.orm import db_session, core

@app.route('/account/registerNewUser', methods=['POST'])
@auth.is_valid_client()
@args.is_exists(body=['email', 'nickName', 'password'])
@db_session
def registerNewUser():
    try:
        user = User(**request.get_json())
        UserConfiguration(user=user.email)
        db.commit()
        return jsonify({'statusCode': 200, 'result': user.to_dict()})
    except ValueError:
        return jsonify({'statusCode': 400, 'result': 'Invalid email: %s' % request.get_json()['email']})
    except core.TransactionIntegrityError:
        return jsonify({'statusCode': 400, 'result': 'Duplicated email: %s' % request.get_json()['email']})


@app.route('/account/emailConfirm', methods=['GET'])
@args.is_exists(body=['token'])
@db_session
def emailConfirm():
    #TODO: Token Management
    return jsonify({'statusCode': 200, 'result': {}})
