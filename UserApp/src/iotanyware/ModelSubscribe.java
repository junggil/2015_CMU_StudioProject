package iotanyware;

public class ModelSubscribe extends java.util.Observable {
	
	public ModelSubscribe(String profile, String sn) {
		/**
		 * basically, I should generated with profile information of SA Nodes
		 * But now we assume that we already know it from account manager.
		 * 
		 * After that it should subscribe the SA node topic
		 */
	}
	public ModelSubscribe() {
		/**
		 * basically, I should generated with profile information of SA Nodes
		 * But now we assume that we already know it from account manager.
		 * 
		 * After that it should subscribe the SA node topic
		 */
		System.out.println("Model: Default Node was creasted");
	}
	
	public void updateDoorStatus(String str){
		System.out.println("Model: Door status = " + str);	

		//model Push - send counter as part of the message
		setChanged();
		notifyObservers(str);
	}	
}
