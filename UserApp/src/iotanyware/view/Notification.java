package iotanyware.view;

import java.io.IOException;
import java.io.StringWriter;

import org.json.simple.JSONObject;

import iotanyware.model.ModelSubscribe;


public class Notification implements State {

	View view;
	
	String moreInfo;
	String nodeId;
	
	boolean update;
	
	public Notification(View view) {
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
	}

	@Override
	public void enterString(String string) {
		// TODO Auto-generated method stub
		if(string.matches("yes")) {
			view.textPane.setText("");
			moreInfo = "";
			
			view.textPane.append("0. Go Node List\n\n");
			view.textPane.append("Alarm will be enabled.\n");
			
			String topic = "/sanode/"+ nodeId +"/control";
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("publisher", view.getUserName());
			jsonObj.put("name", "alarm");
			jsonObj.put("value", "on");
			
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
		if(string.matches("no")) {
			view.textPane.setText("");
			moreInfo = "";
			
			view.textPane.append("0. Go Node List\n\n");
			
			String topic = "/sanode/"+ nodeId +"/control";
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("publisher", view.getUserName());
			jsonObj.put("name", "alarm");
			jsonObj.put("value", "off");
			
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
	}

	@Override
	public void updateState(ModelSubscribe model) {
		// TODO Auto-generated method stub
		
		if(update) {
			update = true;
			return;
		}
		
		view.textPane.setText("");
		moreInfo = "";
		
		view.textPane.append("0. Go Node List\n\n");
		
		for( int i=0; i < model.getNodeNum(); i++ ) {
			if(model.hasMessage(i)) {
				view.textPane.append(" [" + model.getNodeName(i) + "] " + model.getNotificationMsg(i) + view.newline);
				if(model.isAlertMsg(i)) {
					moreInfo = "\n[Alert] Do you want to enable the alarm? [yes/no]\n";
					nodeId = model.getNodeId(i);
				}
			}
		}
		view.textPane.append(moreInfo);
	}
	
}
