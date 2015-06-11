from os import urandom
from binascii import hexlify
from pony.orm.core import ObjectNotFound

def generateKey(keyLength=16):
    return str(hexlify(urandom(int(keyLength / 2))), 'ascii')

def model_unique_id(model, length=16):
    try:
        while True:
            new_id = generateKey(length)
            model[new_id]
    except ObjectNotFound:
        return new_id
