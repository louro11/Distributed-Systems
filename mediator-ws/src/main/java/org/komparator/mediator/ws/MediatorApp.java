package org.komparator.mediator.ws;

import java.util.Timer;
import java.util.TimerTask;


public class MediatorApp {

	private static final int WAITING_RANGE = 5; //in seconds
	
	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length == 0 || args.length == 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + MediatorApp.class.getName() + " wsURL OR uddiURL wsName wsURL");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		//final Timer timer = new Timer(/*isDaemon*/ true);
		
		LifeProof lifeproof = null;
		Timer timer = null;

		// Create server implementation object, according to options
		MediatorEndpointManager endpoint = null;
		if (args.length == 1) {
			wsURL = args[0];
			endpoint = new MediatorEndpointManager(wsURL);
		} else if (args.length >= 3) {
			uddiURL = args[0];
			wsName = args[1];
			wsURL = args[2];
			endpoint = new MediatorEndpointManager(uddiURL, wsName, wsURL);
			endpoint.setVerbose(true);
		}

		try {
			endpoint.start();
			lifeproof = new LifeProof(endpoint);
			timer = new Timer(/*isDaemon*/ true);
			timer.schedule(lifeproof, /*delay*/10  * 1000, /*period*/ WAITING_RANGE * 1000);
	        Thread.sleep(WAITING_RANGE * 1000);
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
			timer.cancel();
			timer.purge();
		}

	}

}
