package iotanyware.model;

import java.util.ArrayList;
import java.util.List;

public class SANode {
	
	boolean owner;
	String nodeId;
	String nodeName;
	
	String displayInfo;
	boolean hasInfo;
	boolean isAlert;
	
	List<SensorActuator> saList;
	
	public SANode() {
		owner = false;
		nodeId = "";
		nodeName = "";
		saList = new ArrayList<SensorActuator>();
		
		hasInfo = false;
		isAlert = false;
	}
	
	public SANode(String strid, String strname, boolean own) {
		nodeId = strid;
		nodeName = strname;
		owner = own;
		saList = new ArrayList<SensorActuator>();
		
		hasInfo = false;
		isAlert = false;
	}
	
	public void setNotificationMessage(String info) {
		displayInfo = info;
		hasInfo = true;
	}
	
	public void setAlert(boolean b) {
		isAlert = b;
	}
	
	public boolean getAlert() {
		return isAlert;
	}
	
	public String getNotificationMessage() {
		return displayInfo;
	}
	
	public void clearNotificationMessage() {
		hasInfo = false;
		isAlert = false;
	}
	
	public boolean hasNotificationMessage() {
		return hasInfo;
	}
	
	public void setOwner(boolean own) {
		owner = own;
	}
	
	public boolean getOwner() {
		return owner;
	}
	
	public void setNodeId(String str) {
		nodeId = str;
	}
	
	public void setNodeName(String str) {
		nodeName = str;
	}
	
	public String getNodeId() {
		return nodeId;
	}
	
	public String getNodeName() {
		return nodeName;
	}
	
	public int getSensorAcuatorNum() {
		return saList.size();
	}
	
	public int addSensorActuator(SensorActuator sa) {
		saList.add(sa);
		return saList.size();
	}
	
	public int removeSensorActuator(SensorActuator sa) {
		int index = -1;
		
		//may be saValue is not synchronous on real time
		for( int i = 0; i < saList.size(); i++) {
			if(sa.getSaName() == saList.get(i).saName) {
				index = i;
				break;
			}
		}
		
		if(index >= 0 ) {
			saList.remove(index);
		}
		
		return saList.size();
	}
	
	public void removeSensorActuator(int idx) {
		if(idx >= saList.size()) {
			System.out.println("========================= ERR: index over: " + idx);
			return;
		}
		saList.remove(idx);
	}
	
	public String getSaName(int idx) {
		return saList.get(idx).getSaName();
	}
	
	public String getSaValue(int idx) {
		return saList.get(idx).getSaValue();
	}
	
	public String getSaProfile(int idx) {
		return saList.get(idx).getSaProfile();
	}
	
	public boolean getSaCanControl(int idx) {
		return saList.get(idx).getCanControl();
	}
}
