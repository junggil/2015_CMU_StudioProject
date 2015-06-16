package iotanyware.view;

import iotanyware.HTTPServerAdapter;
import iotanyware.model.ModelSubscribe;

public class LogView implements State{

	View view;
	boolean update = false;
	
	public LogView(View view) {
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
	}

	@Override
	public void enterString(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateState(ModelSubscribe model) {
		// TODO Auto-generated method stub
		if(update) {
			return;
		}
		update = true;
		
		view.textPane.setText("");
		
		HTTPServerAdapter httpServer = HTTPServerAdapter.getInstance();
		String log = httpServer.viewLog(view.getUserName());
		
		view.textPane.append(log);
		
		view.textPane.append("\n0. Node List (Please enter 0 to go Node List) \n");		
	}

}
