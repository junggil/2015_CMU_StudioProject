from was import app, db
from was.models import *
from was.utils import token, smtp
from was.decorators import args, auth
from urllib.request import urljoin
from flask import request, jsonify, render_template
from pony.orm import db_session, core
from datetime import datetime, timedelta

@app.route('/account/registerNewUser', methods=['POST'])
@auth.is_valid_client()
@args.is_exists(body=['email', 'nickName', 'password'])
@db_session
def registerNewUser():
    try:
        otp = token.generateOtp(lambda otp:PendingRequestUser.get(otpToken=otp))
        url = urljoin(request.base_url.split('account')[0], '/account/emailConfirm?otp=' + otp)
        parameters = request.get_json()
        parameters['otpToken'] = otp

        if User.get(email=parameters['email']):
            return jsonify({'statusCode': 400, 'result': 'Duplicated email: %s' % request.get_json()['email']})

        old_pending_user = PendingRequestUser.get(email=parameters['email'])
        if old_pending_user:
            old_pending_user.delete()
            db.commit()

        PendingRequestUser(**parameters)
        db.commit()
        smtp.sendConfirmMail(parameters['email'], parameters['nickName'], url)
        return jsonify({'statusCode': 200, 'result': {}})
    except ValueError:
        return jsonify({'statusCode': 400, 'result': 'Invalid email: %s' % request.get_json()['email']})


@app.route('/account/emailConfirm', methods=['GET'])
@args.is_exists(body=['otp'])
@db_session
def emailConfirm():
    otp = request.args.get('otp')
    pending_user = PendingRequestUser.get(otpToken=otp)
    if pending_user and pending_user.endDate > datetime.now():
        parameters = pending_user.to_dict()
        del parameters['endDate']
        del parameters['createDate']
        del parameters['otpToken']
        user = User(**parameters)
        UserConfiguration(user=user.email)
        pending_user.delete()
        db.commit()
        return render_template('welcome.html')
    elif pending_user and pending_user.endDate < datetime.now():
        return jsonify({'statusCode': 400, 'result': 'Token is expired'})
    else:
        return jsonify({'statusCode': 400, 'result': 'Invalid otp'})
