from was import app
from daemon import client
from daemon import logger, profile, heartbeat, vnode

client.connect('broker.mqttdashboard.com', 1883, 60)
client.loop_start()

vnode.init_vnode_list()
app.run(host='0.0.0.0', port=8000, debug=False)
