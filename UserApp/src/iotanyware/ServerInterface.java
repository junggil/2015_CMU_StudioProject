package iotanyware;

public interface ServerInterface {
	public abstract boolean registerUser(String email, String password); //after , it should return session id
    public abstract String loginProgess(String email, String password); //after login, it should return session id
    
    public abstract String getNodeList(String sessionid);
    public abstract String getProfile(String uri);   //this is used for get the profile of each sensor/actuator which has SA node.
    
    public abstract boolean registerNode(String sessionid, String nodeid, String nickName);
    public abstract boolean unregisterNode(String sessioinid, String nodeid);
    
    public abstract boolean setLogConfig(String sessionid, int loggingHour);
    public abstract int getLogConfig(String sessioinid);
    
    public abstract String viewLog(String sessioinid);
}
