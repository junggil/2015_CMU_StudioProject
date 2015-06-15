package iotanyware.model;

import java.util.ArrayList;
import java.util.List;

public class ModelSubscribe extends java.util.Observable {
	
	List<SANode> nodeList;
	
	public ModelSubscribe(String profile, String sn) {
		/**
		 * basically, I should generated with profile information of SA Nodes
		 * But now we assume that we already know it from account manager.
		 * 
		 * After that it should subscribe the SA node topic
		 */
		nodeList = new ArrayList<SANode>();
	}
	public ModelSubscribe() {
		/**
		 * basically, I should generated with profile information of SA Nodes
		 * But now we assume that we already know it from account manager.
		 * 
		 * After that it should subscribe the SA node topic
		 */
		System.out.println("Model: Default Node was creasted");
		nodeList = new ArrayList<SANode>();
	}
		
	public void addNewNode(SANode e) {
		nodeList.add(e);
		
		setChanged();
		notifyObservers(nodeList);
	}
	
	public void addNewSensorActuator(String nodeId, SensorActuator sa) {
		for(int i=0; i < nodeList.size(); i++) {
			if(nodeList.get(i).getNodeId().matches(nodeId)) {
				System.out.println("new Sensor or Actuator was added to SANode: " + nodeId);
				nodeList.get(i).addSensorActuator(sa);
				
				setChanged();
				notifyObservers(nodeList);				
			}
		}		
	}
	
	public void addNewSensorActuator(int nodeIdx, SensorActuator sa) {		
		if(nodeIdx >= nodeList.size()) {
			return;
		}
		nodeList.get(nodeIdx).addSensorActuator(sa);		
		//setChanged();
		//notifyObservers(nodeList);
	}
	
	public boolean isAlertMsg(int idx) {
		if(idx >= nodeList.size()) {
			return false;
		}
		return nodeList.get(idx).getAlert();
	}
	
	public void setIsAlertMsg(int idx, boolean b) {
		if(idx >= nodeList.size()) {
			return;
		}
		nodeList.get(idx).setAlert(b);
	}	
	
	public void setIsAlertMsg(String id, boolean b) {
		int idx = findNodeIndexById(id);
		if(idx < 0) {
			return;
		}
		nodeList.get(idx).setAlert(b);
	}
	
	public String getNotificationMsg(int index) {
		if(index < nodeList.size()) {
			return nodeList.get(index).getNotificationMessage();
		}
		return null;
	}
	
	public void setNotificationMsg(String str, String nid) {
		int idx = findNodeIndexById(nid);
		if( idx < 0) {
			return;
		}
		nodeList.get(idx).setNotificationMessage(str);
		
		setChanged();
		notifyObservers(nodeList);		
	}
	
	public boolean hasMessage(int idx) {
		if(idx >= nodeList.size()) {
			return false;
		}
		return nodeList.get(idx).hasNotificationMessage();
	}
	
	public void clearNotification(int idx) {
		if(idx >= nodeList.size()) {
			return;
		}
		nodeList.get(idx).clearNotificationMessage();
	}
	
	public int getSensorActuatorNum(int index) {
		return nodeList.get(index).getSensorAcuatorNum();
	}
	
	public String getSensorActuatorName(int index, int saidx) {
		return nodeList.get(index).getSaName(saidx);
	}	
	
	public String getSensorActuatorValue(int index, int saidx) {
		return nodeList.get(index).getSaValue(saidx);
	}
	
	public String getSensorActuatorProfile(int index, int saidx) {
		return nodeList.get(index).getSaProfile(saidx);
	}
	
	public boolean getSensorActuatorCanControl(int index, int saidx) {
		return nodeList.get(index).getSaCanControl(saidx);
	}
	
	public void removeSensorActuator(int index, int saidx) {
		nodeList.get(index).removeSensorActuator(saidx);
	}
	
	public void triggerViewUpdate() {
		setChanged();
		notifyObservers(nodeList);
	}
	
	public int getNodeNum() {
		return nodeList.size();
	}
	
	public String getNodeName(int index) {
		return nodeList.get(index).getNodeName();
	}
	
	public String getNodeId(int index) {
		return nodeList.get(index).getNodeId();
	}
	
	public int findNodeIndexByName(String name){
		for(int i=0; i< nodeList.size(); i++) {
			if(getNodeName(i).matches(name)) {
				return i;
			}
		}
		return -1;
	}
	
	public int findNodeIndexById(String id) {
		for(int i=0; i< nodeList.size(); i++) {
			if(getNodeId(i).matches(id)) {
				return i;
			}
		}
		return -1;
	}
	
	public void removeNodeById(String id) {
		int idx = findNodeIndexById(id);
		if( idx < 0){
			return;
		}
		nodeList.remove(idx);
	}
	
	public int findSensorActuatorIndex(String name, int idx) {
		for(int i=0; i< getSensorActuatorNum(idx); i++) {
			if( getSensorActuatorName(idx, i).matches(name)) {
				return i;
			}
		}
		return -1;
	}
	
	public void setSensorActuatorValue(int nodeIdx, int saIdx, String value) {
		nodeList.get(nodeIdx).saList.get(saIdx).setSaValue(value);
		
		setChanged();
		notifyObservers(nodeList);
	}
}
