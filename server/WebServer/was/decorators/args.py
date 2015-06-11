from flask import request, jsonify
from functools import wraps

def is_exists(header=[], body=[]):
    def decorator(f):
        @wraps(f)
        def decorated_function(*args, **kwargs):
            try:
                for param in header:
                    if not request.headers.get(param):
                        return jsonify({'statusCode': 400, 'result': 'Missing headers: %s' % param})

                for param in body:
                    if (request.method == 'GET' and not request.args.get(param)) or \
                       (request.method != 'GET' and not request.data) or \
                       (request.method != 'GET' and not request.get_json(force=True).get(param)):
                        return jsonify({'statusCode': 400, 'result': 'Missing parameters: %s' % param})
            except:
                return jsonify({'statusCode': 400, 'result': 'Invalid json type'})
            return f(*args, **kwargs)
        return decorated_function
    return decorator

def is_valid(header={}, body={}):
    def decorator(f):
        @wraps(f)
        def decorated_function(*args, **kwargs):
            try:
                for param in header:
                    if (isinstance(header[param], str)  and request.headers.get(param) != header[param]) or \
                       (isinstance(header[param], list) and request.headers.get(param) not in header[param]):
                        return jsonify({'statusCode': 400, 'result': 'Invalid headers'})

                for param in body:
                    if not request.data or \
                       (isinstance(body[param], str)  and request.get_json(force=True).get(param) != body[param]) or \
                       (isinstance(body[param], list) and request.get_json(force=True).get(param) not in body[param]):
                        return jsonify({'statusCode': 400, 'result': 'Invalid parameters'})
            except:
                return jsonify({'statusCode': 400, 'result': 'Invalid json type'})
            return f(*args, **kwargs)
        return decorated_function
    return decorator
