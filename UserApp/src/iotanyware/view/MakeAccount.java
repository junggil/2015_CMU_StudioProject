package iotanyware.view;

import iotanyware.model.ModelSubscribe;


public class MakeAccount implements State {

	View view;
	public MakeAccount(View view) {
		this.view = view;
	}
	
	@Override
	public void enterNumber(int number) {
		// TODO Auto-generated method stub
		if( number == 1) {
			System.out.println("Change to Make Accout Status");
			view.textPane.setText("");
			
	        String initString[] =
                { "Welcome to IoT Anyware.",
                  "Please select the number what you want to do.",
                  "",
                  "1. Login (Enter like \"id/password\")",
                  "2. Make Account"};
 
	        for (int i = 0; i < initString.length; i ++) {
	        	view.textPane.append(initString[i] + view.newline);
	        }
	        
	        view.setStatus(view.getWelcomeState());
			
		}
		else {
			System.out.println("Invalid Input!");
		}		
	}

	@Override
	public void enterString(String string) {
		// TODO Auto-generated method stub
		/*
		String[] str = string.split("/");

		if( str.length != 3) {
			System.out.println("Invalid input for log-in");
		}
		System.out.println("======================");
		System.out.println(str[0]);
		System.out.println(str[1]);
		System.out.println(str[2]);
		System.out.println("======================");
		
		System.out.println("TODO make account!!!");
		*/
		
		view.textPane.append("\n" + string );
	}

	@Override
	public void updateState(ModelSubscribe model) {
		// TODO Auto-generated method stub
		
	}

}
