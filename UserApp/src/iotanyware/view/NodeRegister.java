package iotanyware.view;

import iotanyware.model.ModelSubscribe;

public class NodeRegister implements State {

	View view;
	public NodeRegister(View view) {
		this.view = view;
	}
	
	@Override
	public void enterNumber(int number) {
		// TODO Auto-generated method stub
		if( number == 0) {
			view.setStatus(view.getNodeList());
		}
	}

	@Override
	public void enterString(String string) {
		// TODO Auto-generated method stub
		view.textPane.append("\n" + string );
	}

	@Override
	public void updateState(ModelSubscribe model) {
		// TODO Auto-generated method stub	
		
		view.textPane.setText("");
        String initString[] =
            { "To register new node:",
              "Please enter the SA node ID and SA node name like",
              "1234/MailBox",
              "",
              "To unregiter the node:",
              "Please enter the SA node ID (serial number)",
              "",
              "If want to return Node list, please enter 0",
              ""};

        for (int i = 0; i < initString.length; i ++) {
        	view.textPane.append(initString[i] + view.newline);
        }  
		
	}

}
