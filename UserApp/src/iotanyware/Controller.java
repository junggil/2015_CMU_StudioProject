package iotanyware;

import iotanyware.model.ModelSubscribe;
import iotanyware.model.SANode;
import iotanyware.model.SensorActuator;
import iotanyware.view.View;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Controller implements KeyListener, MqttCallback {

	ModelSubscribe node;
	View view;
	
	String notifyStr = "";
	
	private String broker = "tcp://broker.mqttdashboard.com:1883";
	
	private MqttClient client;
	private HTTPServerAdapter httpserver;
	
	private String uuid;

	public void addModel(ModelSubscribe m){
		System.out.println("Controller: adding model");
		this.node = m;
	} 

	public void addView(View v){
		System.out.println("Controller: adding view");
		this.view = v;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		displayInfo(e, "KEY PRESSED: ");
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		//displayInfo(e, "KEY RELEASED: ");
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		//displayInfo(e, "KEY TYPED: ");
	} 
	
	private void displayInfo(KeyEvent e, String KeyStatus) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			System.out.println("Enter Key!!!!!");
			
			if(view == null) {
				return;
			}
			
			String inputString = view.getInputString();
						
			view.clearInputText();
			
			String[] str = inputString.split("\n");
	    	int lastStringIndex = str.length;
	    	if(lastStringIndex > 0) {
		    	System.out.println("User Input = " + str[lastStringIndex-1]);
		    	
		    	try {
		    		int number = Integer.parseInt(str[lastStringIndex-1]);
		    		
		    		if(number > 0 && view.getStatus() == view.getNodeRegister()) {
	    				//TODO - Register / Unregister
	    				if( progressRegister(inputString)) {
	    					view.enterString("");
	    				}
	    				else {
	    					view.enterString("Fail!!!");
	    				}    					
	    			}
		    		else if(number == 0 && view.getStatus() == view.getNotification()){
		    			//TODO clear alarm message
		    			for(int i=0; i < node.getNodeNum(); i++) {
		    				node.clearNotification(i);
			    			view.enterNumber(number);
			    			node.triggerViewUpdate();
		    			}
		    		}
		    		else {
		    			view.enterNumber(number);
		    			node.triggerViewUpdate();
		    		}
		    	} catch (NumberFormatException nfe){
		    		System.out.println(str[lastStringIndex-1] + " is not number!");
		    		
		    		//if current view state is for login, it progress the login here
		    		if(view.getStatus() == view.getWelcomeState()) {
		    			if(progressLogin(str[lastStringIndex-1]) < 0) {
		    				System.out.println("Login Failed");
		    				view.addText("Fail!!!");
		    			} 
		    			else {
		    				view.setMqttClientSocket(client);
		    				//after login, get the node list and pass it to view by string.
		    				String strNodeList = "";
		    				for(int i=0; i < node.getNodeNum(); i++) {
		    					strNodeList += node.getNodeId(i);
		    					strNodeList += "/";
		    					strNodeList += node.getNodeName(i);		    					
		    					if(i+1 < node.getNodeNum())
		    						strNodeList += "/";
		    				}
		    				view.enterString(strNodeList);
		    			}
		    		}
		    		else if(view.getStatus() == view.getNotification()) {
		    			if(str[lastStringIndex-1].matches("yes") || str[lastStringIndex-1].matches("no")) {
		    				for(int i=0; i < node.getNodeNum(); i++) {
		    					if(node.isAlertMsg(i)) {
		    						node.clearNotification(i);
		    					}
		    				}
		    			}
		    			view.enterString(str[lastStringIndex-1]);
		    		}
		    		else {		    			
		    			if(view.getStatus() == view.getMakeAccount()) {
		    				//TODO - Make Account
		    				int rtn = progressMakeAccount(str[lastStringIndex-1]);
		    				if(rtn < 0) {
		    					view.enterString("Failed!!!!");
		    				}
		    				else {
		    					view.enterString("Success!!!");
		    				}
		    					
		    			}
		    			else if(view.getStatus() == view.getNodeRegister()) {
		    				//TODO - Register / Unregister
		    				if( progressRegister(inputString)) {
		    					view.enterString("");
		    				}
		    				else {
		    					view.enterString("Fail!!!");
		    				}
		    			}
		    			else {		    			
				    		view.enterString(str[lastStringIndex-1]);
				    		node.triggerViewUpdate();
		    			}
		    		} //if(view.getStatus()
		    	} //try
	    	} //if(lastStringIndex > 0)
	    }//if(e.getKeyCode()
	}
	
	public boolean progressRegister(String input) {
		String[] str;
		String[] newlinestr = input.split("\n");	

		System.out.println("newlien num = " + newlinestr.length);
		if(newlinestr.length > 1) {
			str = newlinestr[1].split("/");
		}
		else {
			str = newlinestr[0].split("/"); //input.split("/");
		}		
		
		httpserver = HTTPServerAdapter.getInstance();
		if(str.length == 1) {
			//TODO unregister
			if(httpserver.unregisterNode(view.getUserName(), str[0])){
				System.out.println("remove node sn = " + str[0] + ".");
				node.removeNodeById(str[0]);
				
				try {
					client.unsubscribe("/node/"+ str[0] + "/status");
					client.unsubscribe("/node/"+ str[0] + "/notify");
				} 
				catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				view.addText("Success!!!\n");
				return true;
			}			
		}
		else if(str.length == 2){
			//TODO register
			if( node.findNodeIndexById(str[0]) < 0 ){
				System.out.println("Try register ID");
			}
			else {
				System.out.println("ID already used!");
				view.addText("Already used one!");
				return true;
			}
			
			if(httpserver.registerNode(view.getUserName(), str[0], str[1])){
				view.addText("Please turn on the SA node, or Press the Button\n");
				return true;
			}
		}
		return false;
	}
	public int progressMakeAccount(String maccount) {
		String[] str;
		String[] newlinestr = maccount.split("\n");	

		System.out.print("newlien num = " + newlinestr.length);
		if(newlinestr.length > 1) {
			str = newlinestr[1].split("/");
		}
		else {
			str = maccount.split("/");
		}
		
		if( str.length != 2) {
			System.out.println("Invalid input for log-in");
			return -1;
		}
		
		httpserver = HTTPServerAdapter.getInstance();
		boolean isOk = httpserver.registerUser(str[0], str[1]);
			
		if(isOk){
			return 0;
		}
		return -1;
	}
	
	public int progressLogin(String loginStr) {
		String[] str;
		String[] newlinestr = loginStr.split("\n");	

		System.out.print("newlien num = " + newlinestr.length);
		if(newlinestr.length > 1) {
			str = newlinestr[1].split("/");
		}
		else {
			str = loginStr.split("/");
		}

		if( str.length != 2) {
			System.out.println("Invalid input for log-in");
			return -1;
		}
		
		httpserver = HTTPServerAdapter.getInstance();
		String sid = httpserver.loginProgess(str[0], str[1]);
		if( sid == null ) {
			System.out.println("Login was failed!");
			view.addText("Fail!!!");
			return -1;
		}
		
		System.out.println("session id = " + sid);		
		
		view.setUserName(sid);
		view.setUserEmail(str[0]);
		initSubscriber(sid, str[0]);
		
		String mynode = httpserver.getNodeList(sid);
		System.out.println("mynode -------------------------------");
		
		nodeListParse(mynode);
		
		return 0;
	}
	
	public int initSubscriber(String sid, String uid) {
		try {
			String mqttUrl = broker;
			String mqttId  = sid;
			client = new MqttClient(mqttUrl, mqttId);
			client.connect();
			client.setCallback(this);

			client.subscribe("/user/" + uid + "/register");
			client.subscribe("/user/" + uid + "/heartbeat");
			
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
	
	public void addSubscribeTopic(String topic) {
		try{
			System.out.println("addSubscribeTopic = " + topic);
			client.subscribe(topic);			
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		System.out.println("---------------------\n\nMQTT Connection Lost\n\n---------------------");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Topic is     :" + arg0);
		System.out.println("Contents is  :" + arg1.toString());
		
		String[] topicStack = arg0.split("/");
		
		System.out.println("Topic 1     :" + topicStack[0]);
		System.out.println("Topic 2     :" + topicStack[1]);
		System.out.println("Topic 3     :" + topicStack[2]);
		System.out.println("Topic 4     :" + topicStack[3]);
				
		if( topicStack.length > 3) {
			if(topicStack[3].matches("status")) {
				System.out.println("SA Node(" + topicStack[0] + ") status was updated");
				messageParse(topicStack[2], arg1.toString());
			}
			else if(topicStack[3].matches("notify")) {
				System.out.println("SA Node(" + topicStack[0] + ") information arrived");
				notificationParse(topicStack[2], arg1.toString());
			}
			else if(topicStack[3].matches("heartbeat")) {
				progressHeartbeat(arg1.toString());
				node.triggerViewUpdate();
			}
			else if(topicStack[3].matches("register")) {
				progressRegisterNotify(arg1.toString());
			}
		}			
	}
	
	public void progressRegisterNotify(String jsonmsg) {
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonmsg);
			System.out.println( jsonObject.get("node") ); 		//nodeID
			System.out.println( jsonObject.get("nickName") ); 	//nodeName
			System.out.println( jsonObject.get("owner") ); 		//nodeOwner
			
			SANode san;		
			san = new SANode((String)jsonObject.get("node"), (String)jsonObject.get("nickName"), (boolean)jsonObject.get("owner"));	
			node.addNewNode(san);
			
			addSubscribeTopic("/sanode/"+ jsonObject.get("node") +"/status");
			addSubscribeTopic("/sanode/"+ jsonObject.get("node") +"/notify");
			
			if(view.getStatus() == view.getNodeRegister()) {
				view.addText("\nSA Node Registeration Success!!!\n");
			}
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void progressHeartbeat(String jsonmsg) {
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonmsg);
			System.out.println( jsonObject.get("node") ); 		//nodeID
			System.out.println( jsonObject.get("nickName") ); 	//nodeName
			System.out.println( jsonObject.get("status") ); 	//nodeName
			
			int nodeIdx = node.findNodeIndexById((String)jsonObject.get("node"));
			int saNum = node.getSensorActuatorNum(nodeIdx);
			for( ; saNum > 0; saNum--) {
				//TODO remove node list
				node.removeSensorActuator(nodeIdx, saNum-1);
			}			
		}
		catch (ParseException e) {
			e.printStackTrace();
		}		
	}
	
	public void messageParse(String nodeId, String msg)	{
		int nodeIdx = node.findNodeIndexById(nodeId);
		System.out.println("SA Node index =" + nodeIdx);
		if(nodeIdx < 0) {
			System.out.println("Oops! There is no that kind of SA Node!!!");
			return;
		}
		
		JSONParser parser = new JSONParser();
		ContainerFactory containerFactory = new ContainerFactory() {
			public Map createObjectContainer() {
				return new LinkedHashMap();
			}
			public List creatArrayContainer() {
				return new LinkedList();
			}
		};
		
		try {
			Map json = (Map)parser.parse(msg, containerFactory);
			Iterator iter = json.entrySet().iterator();
			int saIdx = -1;
			SensorActuator sa;
			while(iter.hasNext()) {
				Map.Entry entry = (Map.Entry)iter.next();
				System.out.println(entry.getKey() + " =>" + entry.getValue());
				
				saIdx = node.findSensorActuatorIndex((String)entry.getKey(), nodeIdx);
				if(saIdx < 0) { //there is no that kind of SA
					sa = new SensorActuator((String)entry.getKey(), (String)entry.getValue(), "default", false);
					node.addNewSensorActuator(nodeIdx, sa);
				}
				else {
					node.setSensorActuatorValue(nodeIdx, saIdx, (String)entry.getValue());
				}
			}		
		} 
		catch (ParseException pe) {
			System.out.println(pe);
		}
		
		node.triggerViewUpdate();		
	}
	
	public void nodeListParse(String mynode) {
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(mynode);
			JSONArray  resultInfoArray = (JSONArray) jsonObject.get("result");
			
			for(int i=0; i<resultInfoArray.size(); i++) {
				JSONObject nodeObj  = (JSONObject) resultInfoArray.get(i);
				System.out.println( nodeObj.get("node") ); 		//nodeID
				System.out.println( nodeObj.get("nickName") ); 	//nodeName
				System.out.println( nodeObj.get("owner") ); 	//nodeOwner
				
				SANode san;		
				san = new SANode((String)nodeObj.get("node"), (String)nodeObj.get("nickName"), (boolean)nodeObj.get("owner"));
				node.addNewNode(san);
				
				addSubscribeTopic("/sanode/"+ nodeObj.get("node") +"/status");
				addSubscribeTopic("/sanode/"+ nodeObj.get("node") +"/notify");
			}
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void notificationParse(String nodeId, String msg) {
		
		JSONParser parser = new JSONParser();
		ContainerFactory containerFactory = new ContainerFactory() {
			public Map createObjectContainer() {
				return new LinkedHashMap();
			}
			public List creatArrayContainer() {
				return new LinkedList();
			}
		};
		
		try {
			Map json = (Map)parser.parse(msg, containerFactory);
			Iterator iter = json.entrySet().iterator();
			int saIdx = -1;
			SensorActuator sa;
			while(iter.hasNext()) {
				Map.Entry entry = (Map.Entry)iter.next();
				System.out.println(entry.getKey() + " =>" + entry.getValue());
				
				String string = (String)entry.getKey();
				if(string.matches("type")) {
					String strType = (String)entry.getValue();
					if(strType.matches("alert")){
						node.setIsAlertMsg(nodeId, true);
					}
				}
				if(string.matches("info") || string.matches("warn")) {				
					node.setNotificationMsg((String)entry.getValue(), nodeId);
				}
			}
		}
		catch (ParseException pe) {
			System.out.println(pe);
		}

		view.setStatus(view.getNotification());
		node.triggerViewUpdate();		
		
	}
}
