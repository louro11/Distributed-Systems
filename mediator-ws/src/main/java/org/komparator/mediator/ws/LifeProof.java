package org.komparator.mediator.ws;

import java.util.TimerTask;



import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;


public class LifeProof extends TimerTask{

	private MediatorEndpointManager endpointManager;
	private MediatorClient prim_client = null; // client to contact primary mediator
	private MediatorClient backup_client = null; // client to contact backup mediator
	
	public LifeProof(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}
	
	private boolean trade = false;
	
	public void run(){
		
		String url = endpointManager.getWsURL();
		String wsid = url.replaceFirst("^http://localhost:807", "");
		wsid = wsid.substring(0, 1);
		if(wsid.equals("1")){
			try {
				backup_client = new MediatorClient("http://localhost:8072/mediator-ws/endpoint");
				backup_client.imAlive();
				System.out.println("\n[Primary Mediator : Sent imAlive] ");
			} catch (MediatorClientException e) {
				System.out.println("Error while creating a Secondary Mediator Client");
			}
			
		}else{
			if(!trade){
				try {
					prim_client = new MediatorClient("http://localhost:8071/mediator-ws/endpoint");
				} catch (MediatorClientException e) {
					System.out.println("Caught exception when creating primary mediator client");
				}
				try{
					prim_client.imAlive();
					System.out.println("\n[Backup Mediator : Sent imAlive] ");
				} catch (Exception e1){
					trade = true;
					System.out.println("Caught exception when contacting primary mediator");
					endpointManager.setWsURL("http://localhost:8072/mediator-ws/endpoint");
					try {
						endpointManager.publishToUDDI();
					} catch (Exception e) {
						System.out.println("Caught exception when publishing to uddi");
					}
				}
			}
			
		}
		
	}
}
