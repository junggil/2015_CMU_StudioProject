package iotanyware.view;

import iotanyware.model.ModelSubscribe;

public interface State {
	
	public void enterNumber(int number);
	public void enterString(String string);
	public void updateState(ModelSubscribe model);
	
}
