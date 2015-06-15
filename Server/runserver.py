from daemon import client
from was import app

client.connect('broker.mqttdashboard.com', 1883, 60)
client.loop_start()

app.run(host='0.0.0.0', port=8000, debug=False)
