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
                  "Input form: [id=email]/[password]",
                  "",
                  "(ex) sam@gmail.com/12341234",
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
        	  "Please select the number what you wants to",
              "",
              "1. Regiter/Unregiter Node",
              "2. Configure",
              "3. View Log",
              "4. Sharing SA node",
              "",
              "Please select the node number, if you want to show detail",
              ""};

        for (int i = 0; i < initString.length; i ++) {
        	view.textPane.append(initString[i] + view.newline);
        }
        
		view.setStatus(view.getNodeList());
		
		System.out.println("node num = " + str.length);
        if(str.length > 1 ) {
	        for (int i = 0, k=5; i < str.length; i += 2, k++) {
	        	view.textPane.append((k) + ". SA Node - " + str[i+1] + view.newline);
	        	
	        	//for update each node status, it will pub. the query topic for each node. 
	        	view.publishMessage("/sanode/" + str[i] +"/query", "{\"publisher\":\"" + view.getUserEmail() + "\"}", 0);
	        }
        }
		
		//view.setStatus(view.getNodeList());
	}

	@Override
	public void updateState(ModelSubscribe model) {
		// TODO Auto-generated method stub
		
	}

}
