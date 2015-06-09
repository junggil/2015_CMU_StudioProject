package iotanyware;

public class HTTPServerAdapter implements ServerInterface{

	@Override
	public String loginProgess(String id, String passwd) {
		// TODO Auto-generated method stub
		return "kim";
	}

	@Override
	public String getNodeList(String user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfile(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNodeNames(String node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean regiterNode(String nodeid) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean unregiterNode(String nodeid) {
		// TODO Auto-generated method stub
		return true;
	}

}
