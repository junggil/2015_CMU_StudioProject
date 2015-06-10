package iotanyware.view;

import iotanyware.model.ModelSubscribe;

public class WelcomeState implements State {

	View view;
	
	public WelcomeState(View view) {
		// TODO Auto-generated constructor stub
		this.view = view;
	}

	@Override
	public void enterNumber(int number) {
		// TODO Auto-generated method stub
	
		if( number == 2) {
			System.out.println("Change to Make Accout Status");
			view.textPane.setText("");
			
	        String initString[] =
                { "Make Account",
                  "Please key-in like example",
                  "If you want to go back to Welcom page, please press 1",
                  "",
                  "Input form: [id]/[password]/[email address]",
                  "",
                  "(ex) hello/123123/sam@gmail.com",
                  "",
                  "1. Go back Welcome page"};
 
	        for (int i = 0; i < initString.length; i ++) {
	        	view.textPane.append(initString[i] + view.newline);
	        }
	        
	        view.setStatus(view.getMakeAccount());
			
		}
		else {
			System.out.println("Invalid Input!");
		}
	}

	@Override
	public void enterString(String string) {
		// TODO Auto-generated method stub
		
		// It was called after login progress, node list was passed like "node#1/node#2......"	
		System.out.println(string);
		String[] str = string.split("/");
	
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
        
        // it should initialize the publisher
        view.initPublisher(view.getUserName());

		view.setStatus(view.getNodeList());
        
        for (int i = 0, k=3; i < str.length; i += 2, k++) {
        	view.textPane.append((k) + ". SA Node - " + str[i+1] + view.newline);
        	
        	//for update each node status, it will pub. the query topic for each node. 
        	view.publishMessage(str[i]+"/query", "{\"publisher\":\"" + view.getUserName() + "\"}", 0);
        }
		
		//view.setStatus(view.getNodeList());
	}

	@Override
	public void updateState(ModelSubscribe model) {
		// TODO Auto-generated method stub
		
	}

}
