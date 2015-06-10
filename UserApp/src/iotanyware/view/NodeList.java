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
		
		if( number > 2) { //node selection
			view.setNodeIndex(number-3);
			view.setStatus(view.getNodeStatus());
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
        	  "Please select the number which wants to show detail",
        	  "If you want to register or unregister, please select 1 or 2",
              "",
              "1. Regiter Node",
              "2. Unregister Node",
              ""};

        for (int i = 0; i < initString.length; i ++) {
        	view.textPane.append(initString[i] + view.newline);
        }
        		
		for(int i=0; i < model.getNodeNum(); i++) {
			view.textPane.append((i+3) +". SA Node - "+ model.getNodeName(i) + view.newline);
		}
	}
}
