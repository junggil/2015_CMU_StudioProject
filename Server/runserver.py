from daemon import client
from daemon import logger, profile, heartbeat, vnode

client.connect('localhost', 1883, 60)
client.loop_start()
vnode.init_vnode_list()

from was import app
app.run(host='0.0.0.0', port=8000, debug=False)
