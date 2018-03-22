package org.komparator.security.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.CryptoUtil;
import pt.ulisboa.tecnico.sdis.cert.CertUtil;

public class CipherHandler implements SOAPHandler<SOAPMessageContext> {
	
	private static final String SECRET_NAME = "creditCardNr";
	private static final String KEY_PASS = "ti7LCU5H";
	private static final String KEYSTORE_PASS = "ti7LCU5H";
	private static final String KEY_ALIAS = "A67_Mediator";

	public CipherHandler() {
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
		handleCipher(context);
		return true;
	}

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void handleCipher(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
	
		System.out.print("CIPHER HANDLER --- ");
		try {
			System.out.print("intercepted ");
			
			if (outbound) {
				System.out.println("OUTbound SOAP message.");
				
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPBody sb = se.getBody();
				
				NodeList children = sb.getFirstChild().getChildNodes();
				
				for (int i = 0; i < children.getLength(); i++) {
					Node argument = children.item(i);
					
					if (argument.getNodeName().equals(SECRET_NAME)) {
	
						PublicKey pbKey = CertUtil.getX509CertificateFromResource("cer/A67_Mediator.cer").getPublicKey();
						String secretArgument = argument.getTextContent();
						ByteArrayOutputStream byteOut = new ByteArrayOutputStream();	
						byteOut.write(secretArgument.getBytes());

						// cipher argument
						System.out.println("Original argument " + SECRET_NAME + ": " + secretArgument);
						byte[] cipheredArgument = CryptoUtil.asymCipher(byteOut.toByteArray(), pbKey);
						String encodedSecretArgument = DatatypeConverter.printBase64Binary(cipheredArgument);
						System.out.println("Encrypted argument " + SECRET_NAME + ": " + encodedSecretArgument);
						
						// save encoded argument
						argument.setTextContent(encodedSecretArgument);
						msg.saveChanges();
					}
				}
				
			} else {
				System.out.println("INbound SOAP message.");
				
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPBody sb = se.getBody();
				
				NodeList children = sb.getFirstChild().getChildNodes();
				
				for (int i = 0; i < children.getLength(); i++) {
					Node argument = children.item(i);
					
					if (argument.getNodeName().equals(SECRET_NAME)) {
						//TODO: get Private Key
						//String keyAlias = (String) smc.get("A67_Mediator");
						
						PrivateKey pvKey = CertUtil.getPrivateKeyFromKeyStoreResource("jks/A67_Mediator.jks", KEYSTORE_PASS.toCharArray(), KEY_ALIAS, KEY_PASS.toCharArray());
						
						String encodedSecretArgument = argument.getTextContent();
						ByteArrayOutputStream byteOut = new ByteArrayOutputStream();	
						byteOut.write(encodedSecretArgument.getBytes());

						// cipher argument
						System.out.println("Encrypted argument " + SECRET_NAME + ": " + encodedSecretArgument);
						byte[] decipheredArgument = CryptoUtil.asymDecipher(byteOut.toByteArray(), pvKey);
						String secretArgument = DatatypeConverter.printBase64Binary(decipheredArgument);
						System.out.println("Original argument " + SECRET_NAME + ": " + secretArgument);
						
						// save encoded argument
						argument.setTextContent(secretArgument);
						msg.saveChanges();
					}
				}
			}
			System.out.println("SOAP message:");
			SOAPMessage message = smc.getMessage();
				message.writeTo(System.out);
				System.out.println("\n"); // add 2 newlines after message, easier for reading

		} catch (SOAPException se) {
			System.out.print("Ignoring SOAPException in handler: ");
			System.out.println(se);
		} catch (IOException e) {
			System.out.print("Ignoring IOException in handler: ");
			System.out.println(e);
		} catch (UnrecoverableKeyException e) {
			System.out.print("Ignoring UnrecoverableKeyException in handler: ");
			System.out.println(e);
		} catch (KeyStoreException e) {
			System.out.print("Ignoring KeyStoreException in handler: ");
			System.out.println(e);
		} catch (CertificateException e) {
			System.out.print("Ignoring CertificateException in handler: ");
			System.out.println(e);
		}
	}
}
