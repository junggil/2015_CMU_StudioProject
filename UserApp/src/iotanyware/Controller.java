package iotanyware;

import iotanyware.model.ModelSubscribe;
import iotanyware.model.SANode;
import iotanyware.model.SensorActuator;
import iotanyware.view.View;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
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
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Controller implements KeyListener, MqttCallback {

	ModelSubscribe node;
	View view;
	
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
			
			String inputString = view.getInputString();
			
			if(view != null){
				view.clearInputText();
			}
			
			String[] str = inputString.split("\n");
	    	int lastStringIndex = str.length;
	    	if(lastStringIndex > 0) {
		    	System.out.println("User Input = " + str[lastStringIndex-1]);
		    	
		    	try {
		    		int number = Integer.parseInt(str[lastStringIndex-1]);
		    		view.enterNumber(number);
		    		node.triggerViewUpdate();
		    	} catch (NumberFormatException nfe){
		    		System.out.println(str[lastStringIndex-1] + " is not number!");
		    		
		    		//if current view state is for login, it progress the login here
		    		if(view.getStatus() == view.getWelcomeState()) {
		    			if(progressLogin(str[lastStringIndex-1]) < 0) {
		    				System.out.println("Login Failed");
		    			} 
		    			else {
		    				view.setMqttClientSocket(client);
		    				//after login, get the node list and pass it to view by string.
		    				String strNodeList = "";
		    				for(int i=0; i < node.getNodeNum(); i++) {
		    					strNodeList += node.getNodeId(i);
		    					strNodeList += "/";
		    					strNodeList += node.getNodeName(i);
		    					addSubscribeTopic(node.getNodeId(i)+"/#");
		    					if(i+1 < node.getNodeNum())
		    						strNodeList += "/";
		    				}
		    				view.enterString(strNodeList);
		    			}
		    		}
		    		else {
			    		view.enterString(str[lastStringIndex-1]);
			    		node.triggerViewUpdate();
		    		} //if(view.getStatus()
		    	} //try
	    	} //if(lastStringIndex > 0)
	    }//if(e.getKeyCode()
	}
	
	public int progressLogin(String loginStr) {
		
		String[] str = loginStr.split("/");

		if( str.length != 2) {
			System.out.println("Invalid input for log-in");
			return -1;
		}
		
		httpserver = new HTTPServerAdapter();
		String sid = httpserver.loginProgess(str[0], str[1]);
		if( sid == null ) {
			System.out.println("Login was failed!");
			return -1;
		}
		
		//TODO node list update. and set the user name.		
		SANode san;		
		san = new SANode("1111", "Simpsons", true);		
		node.addNewNode(san);
		san = new SANode("2222", "SmartMail", true);	
		node.addNewNode(san);
		
		//TODO: userName should use id from login server.
		view.setUserName(str[0]);
		
		//generation UUID
		try {
			NetworkInterface network;
			network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
			byte[] mac = network.getHardwareAddress();
			uuid = mac.toString();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("uuid = "+ uuid);
		return initSubscriber(uuid);
	}
	
	public int initSubscriber(String sid) {
		try {
			String mqttUrl = broker;
			String mqttId  = sid;
			client = new MqttClient(mqttUrl, mqttId);
			client.connect();
			client.setCallback(this);
			
			//client.subscribe("1111/#");
			//client.subscribe("2222/#");
			
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
	
	/*
	public void pubMessage(String topic, String payload) {
		if(client == null) {
			System.out.println("It is not connected to event bus!!!");
			return;
		}
		
		MqttMessage message = new MqttMessage();
		
		message.setPayload(payload.getBytes());
		try {
			client.publish(topic, message);
		} catch (MqttPersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/

	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		System.out.println("---------------------\n\nMQTT Connection Lost\n\n---------------------");
		initSubscriber(uuid);
		view.setMqttClientSocket(client);
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
		if( topicStack.length > 1) {
			if(topicStack[1].matches("status")) {
				System.out.println("SA Node(" + topicStack[0] + ") status was updated");
				messageParse(topicStack[0], arg1.toString());
			}
			else if(topicStack[1].matches("notify")) {
				System.out.println("SA Node(" + topicStack[0] + ") information arrived");
				notificationParse(topicStack[0], arg1.toString());
			}
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
	
	public void notificationParse(String nodeId, String msg) {
		
	}
}
