package iotanyware.view;

import java.io.IOException;
import java.io.StringWriter;

import org.json.simple.JSONObject;

import iotanyware.model.ModelSubscribe;

public class NodeStatus implements State {

	View view;
	boolean update = false;
	
	public NodeStatus(View view) {
		// TODO Auto-generated constructor stub
		this.view = view;
		update = false;
	}

	@Override
	public void enterNumber(int number) {
		// TODO Auto-generated method stub
		if( number == 0) {
			view.setSaIndex(0);
			view.setStatus(view.getNodeList());
			update = false;
		}
		else {
			view.setSaIndex(number-1);
			view.setStatus(view.getNodeControl());			
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
            { "Node Status of ",
              " If you want to control node, please slect the number",
              " 0. Go SA node list.",
              "",
              "Current Status"};
        
		if(!update){
			String topic = "/sanode/" + model.getNodeId(view.getNodeIndex()) + "/query";
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("publisher", view.getUserName());
			
			StringWriter toStr = new StringWriter();
			try {
				jsonObj.writeJSONString(toStr);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String payload = jsonObj.toString();
			view.publishMessage(topic, payload, 0);
			System.out.println("______________________________________________________________");
		}
		update = true;
        
        //Error Node index is over!!!
        if(view.getNodeIndex() >= model.getNodeNum()) {
            for (int i = 1; i < initString.length; i ++) {
            	view.textPane.append(initString[i] + view.newline);
            }
            view.textPane.append(""+ view.newline);
            view.textPane.append(""+ view.newline);
        	view.textPane.append("Oops, select node is not exist!!!"+ view.newline);
        	return;
        }

        //Basin String
        view.textPane.append(initString[0] + model.getNodeName(view.getNodeIndex()) + view.newline);
        for (int i = 1; i < initString.length; i ++) {
        	view.textPane.append(initString[i] + view.newline);
        }
        
        //Node Status
        if(model.getSensorActuatorNum(view.getNodeIndex()) > 0) {
	        for( int i=0; i< model.getSensorActuatorNum(view.getNodeIndex()); i++) {
	        	view.textPane.append( "   - " + model.getSensorActuatorName(view.getNodeIndex(), i) 
	        			                  + " : " + model.getSensorActuatorValue(view.getNodeIndex(), i) + view.newline);
	        }
        }
        else {
        	view.textPane.append("   - It is not available.\n");
        }
        
        //Control list
        view.textPane.append("" + view.newline);
        view.textPane.append("Control List" + view.newline);
        for( int i=0, k=1; i< model.getSensorActuatorNum(view.getNodeIndex()); i++) {
        	if(model.getSensorActuatorCanControl(view.getNodeIndex(), i) ){
        		view.textPane.append( "   " + (k) + ". " + (model.getSensorActuatorName(view.getNodeIndex(), i)).toUpperCase() + view.newline);
        		k++;
        	}
        }
	}

}
