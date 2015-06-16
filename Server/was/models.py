import re
from was import db
from datetime import datetime, timedelta
from pony.orm import Optional, Required, Set, PrimaryKey

class User(db.Entity):
    email        = PrimaryKey(str, py_check=lambda val:re.match(r'[^@]+@[^@]+\.[^@]+', val))
    nickName     = Required(str)
    password     = Required(str)
    mobileNumber = Optional(str, nullable=True)
    profileImage = Optional(str, nullable=True)
    createDate   = Required(datetime, default=datetime.now)
    register     = Set('RegisteredNode', cascade_delete=True)
    pendingReq   = Set('PendingRequestNode', cascade_delete=True)
    config       = Set('UserConfiguration', cascade_delete=True)

class Profile(db.Entity):
    name  = PrimaryKey(str)
    data  = Required(str)
    nodes = Set('NodeProfile', cascade_delete=True)

class Node(db.Entity):
    nodeId        = PrimaryKey(str)
    virtual       = Required(bool, default=False)
    createDate    = Required(datetime, default=datetime.now)
    lastHeartBeat = Optional(datetime)
    profiles      = Set('NodeProfile', cascade_delete=True)
    register      = Set('RegisteredNode', cascade_delete=True)

class NodeProfile(db.Entity):
    node    = Required(Node)
    name    = Required(str)
    profile = Required(Profile)

class RegisteredNode(db.Entity):
    owner      = Required(bool)
    nickName   = Optional(str)
    user       = Required(User)
    node       = Required(Node)
    createDate = Required(datetime, default=datetime.now)

class Client(db.Entity):
    clientId   = PrimaryKey(str)
    createDate = Required(datetime, default=datetime.now)
    enabled    = Required(bool, default=True)
    endDate    = Required(datetime, default=lambda: datetime.now() + timedelta(days=365))

class Session(db.Entity):
    session      = PrimaryKey(str)
    refreshToken = Required(str)
    sessionType  = Required(str, py_check=lambda val:val in ['node', 'user'])
    sessionOwner = Required(str)
    createDate   = Required(datetime, default=datetime.now)
    enabled      = Required(bool, default=True)
    expires      = Required(datetime, default=lambda: datetime.now() + timedelta(hours=2))

class PendingRequestNode(db.Entity):
    nodeId     = PrimaryKey(str)
    createDate = Required(datetime, default=datetime.now)
    endDate    = Required(datetime, default=lambda: datetime.now() + timedelta(minutes=5))
    user       = Required(User)
    nickName   = Optional(str)

class UserConfiguration(db.Entity):
    user        = PrimaryKey(User)
    loggingHour = Required(int, default=72)

class PendingRequestUser(db.Entity):
    email        = PrimaryKey(str, py_check=lambda val:re.match(r'[^@]+@[^@]+\.[^@]+', val))
    createDate   = Required(datetime, default=datetime.now)
    endDate      = Required(datetime, default=lambda: datetime.now() + timedelta(hours=1))
    nickName     = Required(str)
    password     = Required(str)
    otpToken     = Required(str)
    mobileNumber = Optional(str, nullable=True)
    profileImage = Optional(str, nullable=True)
 
db.generate_mapping(create_tables=True) 
