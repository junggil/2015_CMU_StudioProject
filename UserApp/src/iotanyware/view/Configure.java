package iotanyware.view;

import iotanyware.HTTPServerAdapter;
import iotanyware.model.ModelSubscribe;

public class Configure implements State {

	View view;
	boolean update = false;
	
	public Configure(View view) {
		this.view = view;
		update = false;
	}
	@Override
	public void enterNumber(int number) {
		// TODO Auto-generated method stub
		if(number == 0) {
			view.setSaIndex(0);
			view.setStatus(view.getNodeList());
			update = false;
		}
		else {
			HTTPServerAdapter httpServer = new HTTPServerAdapter();
			if(httpServer.setLogConfig(view.getUserName(), number))	{
				view.textPane.setText("Configuration");
				view.textPane.append("\n0. Node List (Please enter 0 to go Node List) \n\n");
				view.textPane.append(" Current config time for logging is " + number + "hr\n");
				view.textPane.append(" What is your new config time?");			
			}
			
		}
	}

	@Override
	public void enterString(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateState(ModelSubscribe model) {
		// TODO Auto-generated method stub
		if(update){
			return;
		}
		update = true;	
		
		view.textPane.setText("Configuration");
		view.textPane.append("\n0. Node List (Please enter 0 to go Node List) \n\n");
		HTTPServerAdapter httpServer = new HTTPServerAdapter();
		int curTime = httpServer.getLogConfig(view.getUserName());
		if(curTime > 0) {
			view.textPane.append(" Current config time for logging is " + curTime + "hr\n");
		}
		view.textPane.append(" What is your new config time?");
	}
}
