package iotanyware;

public interface ServerInterface {
	public abstract String registerUser(String email, String password, String nickName); //after , it should return session id
    public abstract String loginProgess(String email, String password); //after login, it should return session id
    
    public abstract String getNodeList(String sessionid);
    public abstract String getProfile(String uri);   //this is used for get the profile of each sensor/actuator which has SA node.
    public abstract String getNodeNames(String node); //this is used to get an sensor/actuator names which has SA node.
    
    public abstract boolean registerNode(String sessionid, String nodeid);
    public abstract boolean unregisterNode(String sessioinid, String nodeid);
    
    public abstract String viewLog(String sessioinid);
}
