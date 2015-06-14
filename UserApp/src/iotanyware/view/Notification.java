package iotanyware.view;

import iotanyware.model.ModelSubscribe;


public class Notification implements State {

	View view;
	
	public Notification(View view) {
		// TODO Auto-generated constructor stub
		this.view = view;
	}

	@Override
	public void enterNumber(int number) {
		// TODO Auto-generated method stub
		if( number == 0) {
			view.setSaIndex(0);
			view.setStatus(view.getNodeList());
		}
	}

	@Override
	public void enterString(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateState(ModelSubscribe model) {
		// TODO Auto-generated method stub
		
		view.textPane.setText("");
		
		view.textPane.setText("Please enter 0 to go Node List.");
	}

}
