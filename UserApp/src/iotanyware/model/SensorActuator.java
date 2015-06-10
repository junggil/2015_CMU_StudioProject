package iotanyware.model;

public class SensorActuator {
	String saName;
	String saValue;
	String saProfile;
	boolean canControl;
	
	public SensorActuator() {
		saName = "";
		saValue = "";
		saProfile = "";
		canControl = false;
	}
	
	public SensorActuator(String name, String value, String profile, boolean canCtrl) {
		saName = name;
		saValue = value;
		saProfile = profile;
		canControl = canCtrl;
		
		//we assume that we already know the profile.
		if(saName.startsWith("door")) {
			saProfile = "open or close";
			canControl = true;
		}
		else if(saName.startsWith("light")) {
			saProfile = "on or off";
			canControl = true;
		}
		else if(saName.startsWith("alarm")) {
			saProfile = "on or off";
			canControl = true;
		}
	}
	
	public void setSaName(String name) {
		saName = name;
	}
	
	public void setSaValue(String value) {
		saValue = value;
	}
	
	public void setSaProfile(String profile) {
		saProfile = profile;
	}
	
	public void setCanControl(boolean canCtrl) {
		canControl = canCtrl;
	}
	
	public boolean getCanControl() {
		return canControl;
	}
	public String getSaProfile() {
		return saProfile;
	}
	
	public String getSaName() {
		return saName;
	}
	
	public String getSaValue() {
		return saValue;
	}
}
