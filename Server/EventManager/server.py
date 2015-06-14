from callback import client

client.connect('broker.mqttdashboard.com', 1883, 60)
client.loop_forever()
