import paho.mqtt.client as mqtt

def on_connect(client, userdata, flags, rc):
    client.subscribe('/sanode/#')

client = mqtt.Client()
client.on_connect = on_connect

from . import logger
