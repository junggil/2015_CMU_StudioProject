package iotanyware;

import javax.swing.JFrame;

public class Main {    
	
	public static void main(String[] args) {
		
		View view = new View();
		Controller controller = new Controller();
		ModelSubscribe node = new ModelSubscribe();
		
		view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Display the window.
		view.pack();
		view.setVisible(true);
        
		//setup the MVC model - model will be linked dynamically
		view.addController(controller);
		controller.addView(view);
		controller.addModel(node);
		node.addObserver(view); 
	}

}
