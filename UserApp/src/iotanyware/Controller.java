package iotanyware;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Controller implements KeyListener, MqttCallback {

	ModelSubscribe node;
	View view;
	
	private String broker = "tcp://broker.mqttdashboard.com:1883";
	
	private MqttClient client;
	private HTTPServerAdapter httpserver;

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
				view.changeLog.setText("");
			}
			
			String[] str = inputString.split("\n");
	    	int lastStringIndex = str.length;
	    	if(lastStringIndex > 0) {
		    	System.out.println("User Input = " + str[lastStringIndex-1]);
		    	
		    	try {
		    		int number = Integer.parseInt(str[lastStringIndex-1]);
		    		view.enterNumber(number);
		    	} catch (NumberFormatException nfe){
		    		System.out.println(str[lastStringIndex-1] + " is not number!");
		    		
		    		//if current view state is for login, it progress the login here
		    		if(view.getStatus() == view.getWelcomeState()) {
		    			if(progressLogin(str[lastStringIndex-1]) < 0) {
		    				System.out.println("Login Failed");
		    			} 
		    			else {
		    				//after login, get the node list and pass it to view by string.
		    				view.enterString("Node#1/Node#2");
		    			}
		    		}
		    		else {
			    		view.enterString(str[lastStringIndex-1]);
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
		return initSubscriber(sid);
	}
	
	public int initSubscriber(String sid) {
		try {
			String mqttUrl = broker;
			String mqttId  = sid;
			client = new MqttClient(mqttUrl, mqttId);
			client.connect();
			client.setCallback(this);
			client.subscribe("testtopic/#");
			client.subscribe("1234/#");
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;
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
	}
}
