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
		
		view.publishMessage("testtopic/1", "test message", 0);
	}

	@Override
	public void enterString(String string) {
		// TODO Auto-generated method stub
		
		view.publishMessage("testtopic/1", string, 0);
	}

}
