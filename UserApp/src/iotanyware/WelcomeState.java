package iotanyware;

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
		String[] str = string.split("/");
	
		view.textPane.setText("");
		
        String initString[] =
            { "Node List",
        	  "Please select the number which wants to show detail",
              ""};

        for (int i = 0; i < initString.length; i ++) {
        	view.textPane.append(initString[i] + view.newline);
        }
        
        for (int i = 0; i < str.length; i ++) {
        	view.textPane.append((i+1) + ". " + str[i] + view.newline);
        }
		
		view.setStatus(view.getNodeList());
	}

}
