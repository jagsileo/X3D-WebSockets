import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

//For EAI reference, check EAIExample.java in instantreality.org
//This is a modified version
public class EAISocket implements IOCallback {
	private SocketIO socket;	
	private static Boolean isOverValue;
	public static int ctr = 0;
	public static vrml.eai.Browser browser = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			//initialize constructor
			new EAISocket();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally	{
			// Shutdown
			//if (browser != null)
			//	browser.dispose();
		}
	}

	private static void onBrowserChanged(vrml.eai.event.BrowserEvent evt) {
		// Exit the program when there is an error or InstantPlayer gets closed
		switch (evt.getID()) {
			case vrml.eai.event.BrowserEvent.INITIALIZED:
				break;
			case vrml.eai.event.BrowserEvent.SHUTDOWN:
			case vrml.eai.event.BrowserEvent.URL_ERROR:
			case vrml.eai.event.BrowserEvent.CONNECTION_ERROR:
			default:
				System.exit(0);
		}
	}

	private static Boolean onIsOverChanged(vrml.eai.event.VrmlEvent evt) {
		// Change the color of the sphere to red when the mouse pointer is over the
		// sphere, and back to green when it is not
		vrml.eai.field.EventOutSFBool isOver = (vrml.eai.field.EventOutSFBool)evt.getSource();
		return(isOver.getValue());
	}

	public EAISocket() throws Exception {

		//Establish socket connection with server
		socket = new SocketIO();
		socket.connect("http://127.0.0.1:8001/", this);

		// Sends a string to the server.
		socket.send("Hello Server");

		// Initialize the connection with Instant Player
		java.net.InetAddress address = java.net.InetAddress.getByName("localhost");
		browser = vrml.eai.BrowserFactory.getBrowser(address, 4848);

		while(true) {
			// Add a listener to the browser. The listener is an instance of an anonymous class that
			// inherits from vrml.eai.event.BrowserListener and simply calls our onBrowserChanged method
			browser.addBrowserListener(
					new vrml.eai.event.BrowserListener()
					{
					public void browserChanged(vrml.eai.event.BrowserEvent evt)
					{
					EAISocket.onBrowserChanged(evt);
					}
					}
					);

			// Get the isOver event out of the TouchSensor node
			vrml.eai.Node touchSensor = browser.getNode("touchSensor_0");
			vrml.eai.field.EventOutSFBool isOver = (vrml.eai.field.EventOutSFBool)touchSensor.getEventOut("isOver");
			//Add a listener to the isOver event out. The listener is an instance of an anonymous class
			// that inherits from vrml.eai.event.VrmlEventListener and simply calls our onIsOverChanged method
			isOver.addVrmlEventListener(
					new vrml.eai.event.VrmlEventListener() {
					public void eventOutChanged(vrml.eai.event.VrmlEvent evt) {
					isOverValue = EAISocket.onIsOverChanged(evt);
					}
					}
					);
			//Send a socket message regarding hover over to Node JS	
			//System.out.println(is)	
			socket.emit("hover", isOverValue);
							
			//Thread.sleep(100);
		}
	}

	@Override
		public void onMessage(JSONObject json, IOAcknowledge ack) {
			try {
				System.out.println("Server said:" + json.toString(2));
			} 
			catch (JSONException e) {
				e.printStackTrace();
			}
		}


	@Override
		public void onError(SocketIOException socketIOException) {
			System.out.println("an Error occured");
			socketIOException.printStackTrace();
		}

	@Override
		public void onDisconnect() {
			System.out.println("Connection terminated.");
		}

	@Override
		public void onConnect() {
			System.out.println("Connection established");
		}


	@Override
		//Receiver for custom messages from socket server
		public void on(String event, IOAcknowledge ack, Object... args) {			
			System.out.println("BLAAAA...Server triggered event '" + event + "'");
			Object[] arguments = args;
			String[] argList = (arguments[0].toString()).split(":");
			CharSequence cs1 = "point";
			if(argList.length > 0 && argList[0].contains(cs1)) {
				System.out.println(argList[1]);
				argList[1] = argList[1].replace("[","");
				argList[1] = argList[1].replace("]","");
				argList[1] = argList[1].replace("}","");
				String strPos = argList[1].replace(","," ");
				//String[] pos = argList[1].split(",");
				//float[] position = new float[pos.length];
				//for(int i = 0; i<pos.length; i++) {
				//	try{
				//		position[i] = Float.valueOf(pos[i]);
				//		System.out.println(position[i]);
				//	}
				//	catch(NumberFormatException e) {
				//		System.err.println("Illegal input");
				//	}
				//}
				
				buildSpheres(strPos);
			}
						
		}


		public void onMessage(String data, IOAcknowledge ack) {
			System.out.println("Server said: " + data);
		}

	public static void buildSpheres(String position) {
		if(browser != null) {
			ctr++;
			vrml.eai.Node mainGroup = browser.getNode("mainGroup");
			String vrmlString = buildVRML(ctr, position);
			System.out.println(vrmlString);
			vrml.eai.Node[] nodes = browser.createVrmlFromString(vrmlString);
			//created vrml structure, next step is to add it to the scene
			 vrml.eai.field.EventInMFNode addChildren = (vrml.eai.field.EventInMFNode)mainGroup.getEventIn("addChildren");
			 addChildren.setValue(nodes);
			

		}
	}

	public static String buildVRML(int ctr, String position) {
		
		String str = 
		"#VRML V2.0 utf8\nDEF transform_" + ctr + " Transform {\n" +
		"	translation " + position + "\n" +
		"	children [\n" +
		"		DEF touchSensor_" + ctr + " TouchSensor{}\n" +
		"		DEF shape_" + ctr + " Shape {\n" +
		"			appearance Appearance {\n" +
		"				material Material {diffuseColor 0 1 0}\n" +
		"			}\n" +
		"			geometry Sphere {radius 1}\n" + 
		"		}\n" +
		"	]\n"+
		"}";
		return str;
	}
}









