package iotanyware;

public class NodeList implements State {

	View view;
	
	public NodeList(View view) {
		// TODO Auto-generated constructor stub
		this.view = view;
	}

	@Override
	public void enterNumber(int number) {
		// TODO Auto-generated method stub
		
		if( number > 2) { //node selection
			view.setNodeIndex(number-3);
			view.setStatus(view.getNodeStatus());
		}
		else {
			
		}
	}

	@Override
	public void enterString(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateState(ModelSubscribe model) {
		// TODO Auto-generated method stub
		
	}

}
