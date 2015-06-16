from os import path
from flask import Flask
from pony.orm import Database
from logging.handlers import RotatingFileHandler

app = Flask(__name__, static_url_path=path.join(path.dirname(__file__), 'templates'))
db = Database('mysql',  host='localhost', user='iot-anyware', passwd='eaglefive', db='iot-anyware')

from was.views import *
