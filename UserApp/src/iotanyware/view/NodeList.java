package iotanyware.view;

import iotanyware.model.ModelSubscribe;

public class NodeList implements State {

	View view;
	
	public NodeList(View view) {
		// TODO Auto-generated constructor stub
		this.view = view;
	}

	@Override
	public void enterNumber(int number) {
		// TODO Auto-generated method stub
		
		if( number > 3) { //node selection
			view.setNodeIndex(number-4);
			view.setStatus(view.getNodeStatus());
		}
		else if(number == 1) {
			view.setStatus(view.getNodeRegister());
		}
		else if(number == 2) {
			view.setStatus(view.getConfigureState());
		}
		else if(number == 3) {
			view.setStatus(view.getLogViewState());
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
		
        String initString[] =
            { "Node List",
        	  "Please select the number what you wants to",
              "",
              "1. Regiter/Unregiter Node",
              "2. Configure",
              "3. View Log",
              "",
              "Please select the node number, if you show detail",
              ""};

        for (int i = 0; i < initString.length; i ++) {
        	view.textPane.append(initString[i] + view.newline);
        }
        		
		for(int i=0; i < model.getNodeNum(); i++) {
			view.textPane.append((i+4) +". SA Node - "+ model.getNodeName(i) + view.newline);
		}
	}
}
