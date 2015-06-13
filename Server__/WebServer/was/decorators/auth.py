from flask import request, jsonify
from was.models import Client, Session
from functools import wraps
from pony.orm import db_session
from datetime import datetime

def is_valid_client():
    def decorator(f):
        @wraps(f)
        @db_session
        def decorated_function(*args, **kwargs):
            clientId = request.headers.get('x-client-id')
            if not clientId or not Client.get(clientId=clientId) or not Client.get(clientId=clientId).enabled:
                return jsonify({'statusCode': 401, 'result': 'Invalid client id'})
            return f(*args, **kwargs)
        return decorated_function
    return decorator

def is_valid_session():
    def decorator(f):
        @wraps(f)
        @db_session
        def decorated_function(*args, **kwargs):
            session = request.args.get('session') if request.method == 'GET' else request.get_json(force=True)['session']
            session = Session.get(session=session)
            if not session or not session.enabled:
                return jsonify({'statusCode': 401, 'result': 'Invalid session'})
            elif session.expires < datetime.now():
                return jsonify({'statusCode': 401, 'result': 'Expired session'})
            return f(*args, **kwargs)
        return decorated_function
    return decorator
