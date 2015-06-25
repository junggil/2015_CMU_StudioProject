from daemon import client
from daemon import logger, profile, heartbeat, vnode

use_gunicorn = False

client.connect('localhost', 1883, 60)
client.loop_start()
vnode.init_vnode_list()

if use_gunicorn:
    from was import app
    app.run(host='0.0.0.0', port=8000, debug=False)
