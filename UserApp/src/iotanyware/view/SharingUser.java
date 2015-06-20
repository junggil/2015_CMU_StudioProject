package iotanyware.view;

import java.util.ArrayList;
import java.util.List;

import iotanyware.HTTPServerAdapter;
import iotanyware.model.ModelSubscribe;

public class SharingUser implements State {

	View view;
	
	int selectedNodeIdx = -1;
	//String[] strNodeId;
	List<String> list = new ArrayList<String>();
	int maxNodeNum = 0;
	
	public SharingUser (View view) {
		this.view = view;
	}
	
	@Override
	public void enterNumber(int number) {
		// TODO Auto-generated method stub
		if(number == 0) {
			view.setStatus(view.getNodeList());
			selectedNodeIdx = -1;
		}
		else if( number > 0 && number <= maxNodeNum ) {
			selectedNodeIdx = (number - 1);
		}
	}

	@Override
	public void enterString(String string) {
		// TODO Auto-generated method stub		
		if(selectedNodeIdx < 0) {
			System.out.println("not available");			
		}
		else {
			HTTPServerAdapter httpserver = HTTPServerAdapter.getInstance();
			String[] strNodeId = list.toArray(new String[list.size()]);
			
			System.out.println("ID      = " + strNodeId[selectedNodeIdx]);
			System.out.println("session = " + view.getUserName());
			System.out.println("user    = " + string);

			if(httpserver.sharingUser(view.getUserName(), strNodeId[selectedNodeIdx], string)){
				System.out.println("++++++++++++++++++++++++++++");
				view.textPane.append("\nOK!!!" + view.newline);
			}
			else {
				System.out.println("+++++++++++++++++++++++++++++");
				view.textPane.append("\nFail!!!" + view.newline);
			}
		}
	}

	@Override
	public void updateState(ModelSubscribe model) {
		// TODO Auto-generated method stub
		view.textPane.setText("");
		if(selectedNodeIdx < 0 ) {
        String initString[] =
            { "To sharing my node to another user:",
              "   Please enter the SA node",
              "",
              "",
              "0. Return Node list",
              ""};

	        for (int i = 0; i < initString.length; i ++) {
	        	view.textPane.append(initString[i] + view.newline);
	        }

	        maxNodeNum = 0;
	        list.removeAll(list);
	        for(int i = 0; i < model.getNodeNum(); i++) {
	        	if(model.isMyOwnNode(i)) {
	        		list.add(model.getNodeId(i));
	        		maxNodeNum++;
	        		view.textPane.append(maxNodeNum + ". SA node - "+ model.getNodeName(i) + view.newline);		        		
	        	}
	        }
		}
		else {
	        String initString[] =
	            { "Pleasse enter user ID who you want to share the node",
	              "   ex) anyware@gmail.com",
	              "",
	              "",
	              "0. Return Node list",
	              ""};

		        for (int i = 0; i < initString.length; i ++) {
		        	view.textPane.append(initString[i] + view.newline);
		        } 
		        
		}		
	}

}
