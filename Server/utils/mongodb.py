from pymongo import MongoClient

db = MongoClient()['iot-anyware']

def insert_db(post, collection = 'sanode'):
    db[collection].insert_one(post)

def search_db(conditions, collection = 'sanode'):
    return db[collection].find(conditions)
