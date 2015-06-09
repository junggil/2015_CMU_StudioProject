package iotanyware;

public interface ServerInterface {
    public abstract String loginProgess(String id, String passwd); //after login, it should return session id
    
    public abstract String getNodeList(String user);
    public abstract String getProfile(String uri);   //this is used for get the profile of each sensor/actuator which has SA node.
    public abstract String getNodeNames(String node); //this is used to get an sensor/actuator names which has SA node.
    
    public abstract boolean regiterNode(String nodeid);
    public abstract boolean unregiterNode(String nodeid);
}
