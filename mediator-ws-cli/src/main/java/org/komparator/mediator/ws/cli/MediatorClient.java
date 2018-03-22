package org.komparator.mediator.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import org.komparator.mediator.ws.*;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;


/**
 * Client.
 *
 * Adds easier endpoint address configuration and 
 * UDDI lookup capability to the PortType generated by wsimport.
 */
public class MediatorClient implements MediatorPortType {


	private static final int RECEIVE_TIMEOUT = 5; //in seconds
	private static final int CONNECTION_TIMEOUT = 3; //in seconds  //change??
	
    /** WS service */
    MediatorService service = null;

    /** WS port (port type is the interface, port is the implementation) */
    MediatorPortType port = null;

    /** UDDI server URL */
    private String uddiURL = null;

    /** WS name */
    private String wsName = null;

    /** WS endpoint address */
    private String wsURL = null; // default value is defined inside WSDL

    public String getWsURL() {
        return wsURL;
    }

    /** output option **/
    private boolean verbose = false;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }


    /** constructor with provided web service URL */
    public MediatorClient(String wsURL) throws MediatorClientException {
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
        this.wsURL = wsURL;
        createStub();
    }

    /** constructor with provided UDDI location and name */
    public MediatorClient(String uddiURL, String wsName) throws MediatorClientException {
		if (uddiURL == null || wsName == null)
			throw new NullPointerException("UDDI Address or Web Service Name cannot be null!");
        this.uddiURL = uddiURL;
        this.wsName = wsName;
        uddiLookup();
        createStub();
    }

    /** UDDI lookup */
    private void uddiLookup() throws MediatorClientException {
        try {
            if (verbose)
                System.out.printf("Contacting UDDI at %s%n", uddiURL);
            UDDINaming uddiNaming = new UDDINaming(uddiURL);

            if (verbose)
                System.out.printf("Looking for '%s'%n", wsName);
            wsURL = uddiNaming.lookup(wsName);

        } catch (Exception e) {
            String msg = String.format("Client failed lookup on UDDI at %s!",
                    uddiURL);
            throw new MediatorClientException(msg, e);
        }

        if (wsURL == null) {
            String msg = String.format(
                    "Service with name %s not found on UDDI at %s", wsName,
                    uddiURL);
            throw new MediatorClientException(msg);
        }
    }

    /** Stub creation and configuration */
    private void createStub() {
        if (verbose)
            System.out.println("Creating stub ...");
        
        service = new MediatorService();
        port = service.getMediatorPort();

        if (wsURL != null) {
            if (verbose)
                System.out.println("Setting endpoint address ...");
            BindingProvider bindingProvider = (BindingProvider) port;
            Map<String, Object> requestContext = bindingProvider
                    .getRequestContext();
            requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
            
            /** SET CONNECTION TIME **/
            int connectionTimeout = CONNECTION_TIMEOUT * 1000;
            
            final List<String> CONN_TIME_PROPS = new ArrayList<String>();
            CONN_TIME_PROPS.add("com.sun.xml.ws.connect.timeout");
            CONN_TIME_PROPS.add("com.sun.xml.internal.ws.connect.timeout");
            CONN_TIME_PROPS.add("javax.xml.ws.client.connectionTimeout");
            
            for (String propName : CONN_TIME_PROPS)
            	requestContext.put(propName, connectionTimeout);
            System.out.printf("Set connection timeout to %d milliseconds%n", connectionTimeout);
            
            /** SET RECEIVE TIMEOUT**/
            int receiveTimeout = RECEIVE_TIMEOUT * 1000;
            
            final List<String> RECV_TIME_PROPS = new ArrayList<String>();
            RECV_TIME_PROPS.add("com.sun.xml.ws.request.timeout");
            RECV_TIME_PROPS.add("com.sun.xml.internal.ws.request.timeout");
            RECV_TIME_PROPS.add("javax.xml.ws.client.receiveTimeout");
            
            for (String propName : RECV_TIME_PROPS)
            	requestContext.put(propName, receiveTimeout);
            System.out.printf("Set receive timeout to %d mimlliseconds%n", receiveTimeout);
            	   
        }
    }
    
    private void reLookup(){
    	System.out.println("Lost contact to mediator, trying to obtain new wsURL..");
    	try{
    		uddiLookup();
    	} catch(Exception e) {
    		System.out.println("Failed to obtain new wsURL");
    		System.out.println(e.getMessage());
    	}
    	createStub(); 	
    }


    // remote invocation methods ----------------------------------------------
    
    @Override
	public void clear() {
    	try{
    		port.clear();
    	} catch(WebServiceException wse) {
    		Throwable cause = wse.getCause();
    		if (cause != null && cause instanceof SocketTimeoutException)
    			System.out.println("The cause was a timeout exception: " + cause);
    		reLookup();
    		port.clear();
    	}
	}

    @Override
	public String ping(String arg0) {
    	
    	String result = null;
    	try{
    		result = port.ping(arg0);
    	} catch(WebServiceException wse){
    		Throwable cause = wse.getCause();
    		if(cause != null && cause instanceof SocketTimeoutException) 
    			System.out.println("The cause was a timeout exception: " + cause);
    		reLookup();
    		result = port.ping(arg0);
    	}
    	return result;
	}

    @Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
    	
    	List<ItemView> result = null;
    	try{
    		result = port.searchItems(descText);
    	} catch(WebServiceException wse){
    		Throwable cause = wse.getCause();
    		if(cause != null && cause instanceof SocketTimeoutException)
    			System.out.println("The cause was a timeout exception: " + cause);
    		reLookup();
    		result = port.searchItems(descText);
    	}
    	return result;
	}

    @Override
	public List<CartView> listCarts() {
    	
    	List<CartView> result = null;
    	try{
    		result = port.listCarts();
    	} catch(WebServiceException wse){
    		Throwable cause = wse.getCause();
    		if (cause != null && cause instanceof SocketTimeoutException)
    			System.out.println("The cause was a timeout exception: " + cause);
    		reLookup();
    		result = port.listCarts();
    	}
    	return result;
	}

	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		
		List<ItemView> result = null;
		try{
			result = port.getItems(productId);
		} catch(WebServiceException wse){
			Throwable cause = wse.getCause();
			if (cause != null && cause instanceof SocketTimeoutException)
				System.out.println("The cause was a timeout exception: " + cause);
			reLookup();
			result = port.getItems(productId);
		}
		return result;
	}

	@Override
	public ShoppingResultView buyCart(String cartId, String creditCardNr)
			throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		
		ShoppingResultView result = null;
		try{
			result = port.buyCart(cartId, creditCardNr);
		} catch(WebServiceException wse){
			Throwable cause = wse.getCause();
			if (cause != null && cause instanceof SocketTimeoutException)
				System.out.println("The cause was a timeout exception: " + cause);
			reLookup();
			result = port.buyCart(cartId, creditCardNr);
		}
		return result;
	}

	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		
		try{
			port.addToCart(cartId, itemId, itemQty);
		} catch(WebServiceException wse){
			Throwable cause = wse.getCause();
			if (cause != null && cause instanceof SocketTimeoutException)
				System.out.println("The cause was a timeout exception: " + cause);
			reLookup();
			port.addToCart(cartId, itemId, itemQty);
		}
	}

	@Override
	public List<ShoppingResultView> shopHistory() {
		
		List<ShoppingResultView> result = null;
		try{
			result = port.shopHistory();
		} catch(WebServiceException wse){
			Throwable cause = wse.getCause();
			if (cause != null && cause instanceof SocketTimeoutException)
				System.out.println("The cause was a timeout exception: " + cause);
			reLookup();
			result = port.shopHistory();
		}
		return result;
	}

	@Override
	public void imAlive() {
		
		try{
			port.imAlive();
		} catch(WebServiceException wse){
			Throwable cause = wse.getCause();
			if (cause != null && cause instanceof SocketTimeoutException)
				System.out.println("The cause was a timeout exception: " + cause);
			reLookup();
			port.imAlive();		
		}
	}

	@Override
	public void updateShopHistory() {
		try{
			port.updateShopHistory();
		} catch(WebServiceException wse){
			Throwable cause = wse.getCause();
			if (cause != null && cause instanceof SocketTimeoutException)
				System.out.println("The cause was a timeout exception: " + cause);
			reLookup();
			port.updateShopHistory();
		}
	}

	@Override
	public void updateCart() {
		try{
			port.updateCart();
		} catch(WebServiceException wse){
			Throwable cause = wse.getCause();
			if (cause != null && cause instanceof SocketTimeoutException)
				System.out.println("The cause was a timeout exception: " + cause);
			reLookup();
			port.updateCart();
		}
	}


}