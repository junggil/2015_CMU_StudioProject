package iotanyware.view;

import iotanyware.model.ModelSubscribe;

public class NodeStatus implements State {

	View view;
	
	public NodeStatus(View view) {
		// TODO Auto-generated constructor stub
		this.view = view;
	}

	@Override
	public void enterNumber(int number) {
		// TODO Auto-generated method stub
		
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
            { "Node Status of ",
              "If you want to control node, please slect the number",
              "",
              "Current Status"};

        //Basin String
        view.textPane.append(initString[0] + model.getNodeName(view.getNodeIndex()) + view.newline);
        for (int i = 1; i < initString.length; i ++) {
        	view.textPane.append(initString[i] + view.newline);
        }
        
        //Node Status
        for( int i=0; i< model.getSensorActuatorNum(view.getNodeIndex()); i++) {
        	view.textPane.append( "   - " + model.getSensorActuatorName(view.getNodeIndex(), i) 
        			                  + " : " + model.getSensorActuatorValue(view.getNodeIndex(), i) + view.newline);
        }
        
        //Control list
        view.textPane.append("" + view.newline);
        view.textPane.append("Control, Setting, or Configure" + view.newline);
        for( int i=0, k=1; i< model.getSensorActuatorNum(view.getNodeIndex()); i++) {
        	if(model.getSensorActuatorCanControl(view.getNodeIndex(), i) ){
        		view.textPane.append( (k) + ". " + (model.getSensorActuatorName(view.getNodeIndex(), i)).toUpperCase() + view.newline);
        		k++;
        	}
        }
	}

}
