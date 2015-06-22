from was import app
from flask import request, jsonify, render_template

@app.route('/', methods=['GET'])
def landingPage():
    return render_template('index.html')
