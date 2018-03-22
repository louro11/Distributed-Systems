package org.komparator.security.handler;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.CryptoUtil;

import pt.ulisboa.tecnico.sdis.cert.CertUtil;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;

public class MediatorSecurityHandler implements SOAPHandler<SOAPMessageContext> {

	private ArrayList<String> usedNonces = new ArrayList<String>();
	
	//private CA CAPort = new CAImplService().getCAImplPort();
	private static final String KEY_PASS = "ti7LCU5H";
	private static final String KEYSTORE_PASS = "ti7LCU5H";
	private static final String KEYSTORE_PATH = "jks/A67_Mediator.jks";
	private static String KEY_ALIAS = "a67_mediator";
	
	public MediatorSecurityHandler() {
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
		try {
			return logToSystemOut(context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	private boolean logToSystemOut(SOAPMessageContext smc) throws Exception{
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
	
		System.out.print("SECURITY HANDLER --- ");
		try {
			
			System.out.print("intercepted ");
			if (outbound) {
				System.out.print("OUTbound ");
				
				// get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();

                SOAPBody sb = se.getBody();
                if (sb == null)
                    sb = se.addBody();

                // add body element (name, namespace prefix, namespace)
                Name nonceName = se.createName("nonce", "ns", "http://demo/");
                SOAPBodyElement nonceElement = sb.addBodyElement(nonceName);

                // add header element value
                nonceElement.addTextNode(generateNonce());
                

                SOAPHeader sh = se.getHeader();
                if (sh == null)
                    sh = se.addHeader();

                // add header element (name, namespace prefix, namespace)
                Name digest = se.createName("digest", "ns", "http://demo/");
                SOAPHeaderElement digestElement = sh.addHeaderElement(digest);

                Name name = se.createName("name", "ns", "http://demo/");
                SOAPHeaderElement nameElement = sh.addHeaderElement(name);

                
				PrivateKey privateKey = CertUtil.getPrivateKeyFromKeyStoreResource(KEYSTORE_PATH,KEYSTORE_PASS.toCharArray(),
						KEY_ALIAS,KEY_PASS.toCharArray());
                nameElement.addTextNode(KEY_ALIAS);
                
                System.out.println();
                
                // sign body
                String bodySignature = DatatypeConverter.printBase64Binary(CryptoUtil.makeDigitalSignature(sb.toString().getBytes(), privateKey));
                digestElement.addTextNode(bodySignature);
				
			} else {
				System.out.print("INbound ");
				
				//get SOAP envelop header
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPBody sb = se.getBody();
				
				SOAPHeader sh = se.getHeader();
				//check header
				if (sh == null) {
					System.out.println("Header not found.");
					return true; //change method to boolean?
				}
				
				//check body
				if (sb == null) {
					System.out.println("Body not found.");
					return true; //change method to boolean?
				}
				
				//get digest
				Name elem = se.createName("digest", "ns", "http://demo/");
				Iterator it = sh.getChildElements(elem);
				//check header element
				if (!it.hasNext()){
					System.out.println("Header digest element not found.");
					return false; 
				}
				SOAPElement element = (SOAPElement) it.next();
				String digest = element.getValue();
				
				//get name
				elem = se.createName("name", "ns", "http://demo/");
				it = sh.getChildElements(elem);
				//check header element 
				if (!it.hasNext()){
					System.out.println("Header name element not found.");
					return false;
				}
				element = (SOAPElement) it.next();
				String name = element.getValue(); 
				
				CAClient cli = new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca");
		        String certificateName = cli.getCertificate(name);
		        
		        if(certificateName==null){
		        	throw new RuntimeException("Certificate is null");
		        }
		    
		        Certificate c = CertUtil.getX509CertificateFromPEMString(certificateName);
		        
				boolean isValidSignature = CryptoUtil.verifyDigitalSignature(parseBase64Binary(digest), sb.toString().getBytes(), c.getPublicKey()); 
				
				if(!isValidSignature){
					System.out.println("Message received is invalid.");
					return false; 
				}
				
				System.out.println("Message received is valid.");
				
				//get first body element
				elem = se.createName("nonce", "ns", "http://demo/");
				it = sb.getChildElements(elem);
				//check body element
				if (!it.hasNext()) {
					System.out.println("nonce element not found.");
					return false; 
				}
				element = (SOAPElement) it.next();
				String nonce = element.getValue();
				
				if(!validNonce(nonce)){
					System.out.println("Message is being repeated.");
					return false;
				}
			}
			System.out.println("SOAP message:");
			SOAPMessage message = smc.getMessage();
				message.writeTo(System.out);
				System.out.println("\n"); // add 2 newlines after message, easier for reading

		} catch (SOAPException se) {
			System.out.print("Ignoring SOAPException in handler: ");
			System.out.println(se);
		} catch (IOException ioe) {
			System.out.print("Ignoring IOException in handler: ");
			System.out.println(ioe);
		}
		
		return true;
		
	}
	
	public String generateNonce() throws NoSuchAlgorithmException {
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); //??
		final byte array[] = new byte[16];
		random.nextBytes(array);

		String nonce = DatatypeConverter.printBase64Binary(array);

		if(usedNonces.contains(nonce))
			return generateNonce();

		usedNonces.add(nonce);
		return nonce;
	}


	public boolean validNonce(String nonce) {
		if(usedNonces.contains(nonce)){
			return false;
		}
		usedNonces.add(nonce);
		return true;
	}
}
