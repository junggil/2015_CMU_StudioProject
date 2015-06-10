package iotanyware.view;

import java.io.IOException;
import java.io.StringWriter;

import org.json.simple.JSONObject;

import iotanyware.model.ModelSubscribe;

public class NodeControl implements State {

	View view;
	
	String validStr;
	String topicId;
	String saName;
	
	public NodeControl(View view) {
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
		if( number == 1) {
			view.setSaIndex(0);
			view.setStatus(view.getNodeStatus());
		}
	}

	@Override
	public void enterString(String string) {
		// TODO Auto-generated method stub
		
		//if you need to check the input string, please compare to validStr
		
		String topic = topicId + "/control";
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("publisher", view.getUserName());
		jsonObj.put("name", saName);
		jsonObj.put("value", string);
		
		StringWriter toStr = new StringWriter();
		try {
			jsonObj.writeJSONString(toStr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String payload = jsonObj.toString();
		view.publishMessage(topic, payload, 0);
	}

	@Override
	public void updateState(ModelSubscribe model) {		
		// TODO Auto-generated method stub
		view.textPane.setText("");
		
        String initString[] =
            { "Node Control of ",
              " If you press the 0, you can go SA node list.",
              " If you press the 1, you can go SA node status.",
              ""};
        
        //Error Node index is over!!!
        if(view.getSaIndex() >= model.getSensorActuatorNum(view.getNodeIndex())) {
            for (int i = 1; i < initString.length; i ++) {
            	view.textPane.append(initString[i] + view.newline);
            }
            view.textPane.append(""+ view.newline);
            view.textPane.append(""+ view.newline);
        	view.textPane.append("Oops, select number is not valid!!!"+ view.newline);
        	return;
        }

        //Basic String
        view.textPane.append(initString[0] + model.getNodeName(view.getNodeIndex()) + view.newline);
        for (int i = 1; i < initString.length; i ++) {
        	view.textPane.append(initString[i] + view.newline);
        }
        
        //Show the current status        
        view.textPane.append( "current status : " + 
        						(model.getSensorActuatorName(view.getNodeIndex(), view.getSaIndex())) + " = " +
        						(model.getSensorActuatorValue(view.getNodeIndex(), view.getSaIndex())) +
        						view.newline);  
        
        view.textPane.append(view.newline);  
        
        //show the how to input.
        topicId = model.getNodeId(view.getNodeIndex());
        saName = model.getSensorActuatorName(view.getNodeIndex(), view.getSaIndex());
		validStr = model.getSensorActuatorProfile(view.getNodeIndex(), view.getSaIndex());
        view.textPane.append( "Is new value  " + validStr + "?" + view.newline);
        
        view.textPane.append("\nIf OK, current status will be changed new one" + view.newline);
	}
}
