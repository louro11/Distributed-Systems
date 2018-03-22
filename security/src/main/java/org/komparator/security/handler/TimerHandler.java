package org.komparator.security.handler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class TimerHandler implements SOAPHandler<SOAPMessageContext> {

	public TimerHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void close(MessageContext arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean handleFault(SOAPMessageContext arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		logToSystemOut(context);
		return true;
	}

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	private void logToSystemOut(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		Date dateout;
	
		System.out.print("TIMER HANDLER --- ");
		try {
			
			System.out.print("intercepted ");
			if (outbound) {
				dateout = new Date();
				String time = dateFormatter.format(dateout);
				System.out.print("OUTbound ");
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();
				
				Name name = se.createName("timestamp", "d", "http://demo");
				SOAPHeaderElement element = sh.addHeaderElement(name);
				element.addTextNode(time);
			} else {
				
				System.out.println("INbound SOAP message:");
				Date datein = new Date();
				
				// get SOAP envelope header
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();
				
				// check header existence
				
				/*
				if (sh == null) {
					System.out.println("Header not found.");
					throw new RuntimeException();
				}
				
				// get first header element
				Name name = se.createName("timestamp", "d", "http://demo");
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					System.out.println("Header element not found.");
					throw new RuntimeException();
				}
				SOAPElement element = (SOAPElement) it.next();
	
				// get header element value
				String timestamp = element.getValue();
				
				dateout = dateFormatter.parse(timestamp);
				diff = datein.getTime() - dateout.getTime();
				if(diff>3000){ //3 seconds = 3000milliseconds
					System.out.println("Time limit exceeded, throwing run-time exception");
					throw new RuntimeException();
				}
				*/
			}
	
			System.out.println("SOAP message:");
			SOAPMessage message = smc.getMessage();
			message.writeTo(System.out);
			System.out.println("\n"); // add 2 newlines after message

		} catch (SOAPException se) {
			System.out.print("Ignoring SOAPException in handler: ");
			System.out.println(se);
		} catch (IOException ioe) {
			System.out.print("Ignoring IOException in handler: ");
			System.out.println(ioe);
		/*} catch (ParseException e) {
			System.out.print("Ignoring ParseException in handler: ");
			System.out.println(e);*/
		}
	}
}
