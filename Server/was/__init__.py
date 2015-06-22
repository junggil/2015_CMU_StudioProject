from os import path
from flask import Flask
from pony.orm import Database
from logging.handlers import RotatingFileHandler

app = Flask(__name__)
db = Database('mysql',  host='localhost', user='iot-anyware', passwd='eaglefive', db='iot-anyware')

from was.views import *
