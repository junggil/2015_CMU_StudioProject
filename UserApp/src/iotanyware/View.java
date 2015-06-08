package iotanyware;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyListener;
import java.util.Observable;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyledDocument;

import iotanyware.DocumentSizeFilter;
import iotanyware.View;

public class View extends JFrame implements java.util.Observer{
    
	static final int MAX_CHARACTERS = 300;
    
    private WelcomeState welcome;
    private NodeStatus status;
    private NodeControl control;
    private NodeList nodeList;
    private Notification notification;
    private MakeAccount makeAccout;
    private NodeRegister register;
    
    State state;
    
	JTextArea textPane;
    JTextPane changeLog;
    AbstractDocument doc;
    String newline = "\n";
         
    public View() {
        super("IoT Anyware");
 
        //Create the text pane and configure it.
        textPane = new JTextArea(5, 30);
        textPane.setCaretPosition(0);
        textPane.setMargin(new Insets(5,5,5,5));
        
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        textPane.setEditable(false);
        
        Font font = new Font("Lucida Sans", Font.BOLD,  15);
        textPane.setFont(font);
 
        //Create the text area for the status log and configure it.
        changeLog = new JTextPane();
        changeLog.setEditable(true);
        JScrollPane scrollPaneForLog = new JScrollPane(changeLog);
        changeLog.setFont(font);
        
        StyledDocument styledDoc = changeLog.getStyledDocument();
        if (styledDoc instanceof AbstractDocument) {
            doc = (AbstractDocument)styledDoc;
            doc.setDocumentFilter(new DocumentSizeFilter(MAX_CHARACTERS));
        } else {
            System.err.println("Text pane's document isn't an AbstractDocument!");
            System.exit(-1);
        }
 
        //Create a split pane for the change log and the text area.
        JSplitPane splitPane = new JSplitPane(
                                       JSplitPane.VERTICAL_SPLIT,
                                       scrollPane, scrollPaneForLog);
        splitPane.setOneTouchExpandable(true);
  
        //Add the components.
        getContentPane().add(splitPane, BorderLayout.CENTER);

 
        //Put the initial text into the text pane.
        initDocument();
        textPane.setCaretPosition(0);
        
        welcome = new WelcomeState(this);
        status = new NodeStatus(this);
        control = new NodeControl(this);
        nodeList = new NodeList(this);
        notification = new Notification(this);
        makeAccout = new MakeAccount(this);
        register = new NodeRegister(this);
        
        state = welcome;
    }

    protected void initDocument() {
        String initString[] =
                { "Welcome to IoT Anyware.",
                  "Please select the number what you want to do.",
                  "",
                  "1. Login (Enter like \"id/password\")",
                  "2. Make Account"};
 
        for (int i = 0; i < initString.length; i ++) {
        	textPane.append(initString[i] + newline);
        }
        
    }
    
	public void addController(KeyListener controller){
		System.out.println("View : adding controller");
		changeLog.addKeyListener(controller);
	}
	
	public String getInputString() {
		return changeLog.getText();
	}
	
	public void enterNumber(int number) {
		state.enterNumber(number);
	}
	
	public void enterString(String string) {
		state.enterString(string);
	}
	
	public void setStatus(State state) {
		this.state = state;
	}
	
	public State getStatus() {
		return this.state;
	}
	
    public State getWelcomeState() {
    	return welcome;
    }
    public State getNodeStatus() {
    	return status;
    }
    
    public State getNodeControl() {
    	return control;
    }

    public State getNodeList() {
    	return nodeList;
    }

    public State getNotification() {
    	return notification;
    }

    public State getMakeAccount() {
    	return makeAccout;
    }
    
    public State getNodeRegister() {
    	return register;
    }
    
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		System.out.println("Node Status updated!!!");
	}
}