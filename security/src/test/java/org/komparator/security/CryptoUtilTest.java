package org.komparator.security;

import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.xml.bind.DatatypeConverter;

//import java.security.*;
import org.junit.*;

import pt.ulisboa.tecnico.sdis.cert.CertUtil;

public class CryptoUtilTest {

    // static members
	final static String CERTIFICATE = "example.cer";
	final static String KEYSTORE = "src/test/resources/example.jks";
	final static String KEYSTORE_PASS = "1nsecure";
	final static String KEY_ALIAS = "example.jks";
	final static String KEY_PASS = "1ns3cur3";
	private final String plainText = "This is the plain text!";
	private final byte[] plainBytes = plainText.getBytes();
	private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
	private static final String PADDING = "RSA/ECB/PKCS1Padding";
    
	/**
	 * test cipher and decipher
	 */
	@Test
	public void testCipher() throws Exception {
		System.out.print("TEST CIPHER with");
		System.out.print(PADDING);
		System.out.print("padding:");
		
		System.out.print("Text: ");
		System.out.println(plainText);
		System.out.print("Bytes: ");
		System.out.println(printHexBinary(plainBytes));
		
		// cipher
		PublicKey publicKey = CertUtil.getX509CertificateFromResource(CERTIFICATE).getPublicKey();
		byte[] cipheredArgument = CryptoUtil.asymCipher(plainBytes, publicKey);
		assertNotNull(cipheredArgument);
		
		System.out.print("Encoded text: ");
		String cipheredText = DatatypeConverter.printBase64Binary(cipheredArgument);
		System.out.println(cipheredText);
		System.out.print("Encoded text Bytes: ");
		System.out.println(cipheredArgument);
		
		// decipher
		PrivateKey privateKey = CertUtil.getPrivateKeyFromKeyStoreResource(KEYSTORE,
				KEYSTORE_PASS.toCharArray(), KEY_ALIAS, KEY_PASS.toCharArray());
		byte[] decipheredArgument = CryptoUtil.asymDecipher(cipheredArgument, privateKey);
		assertNotNull(decipheredArgument);
		
		System.out.print("Decoded text: ");
		String decipheredText = DatatypeConverter.printBase64Binary(decipheredArgument);
		System.out.println(decipheredText);
		System.out.print("Decoded text Bytes: ");
		System.out.println(decipheredArgument);
		
		// verify original with ciphered and then deciphered
		assertEquals(plainBytes, decipheredArgument);
		assertEquals(plainText, decipheredText);
		
		byte[] secondcipheredArgument = CryptoUtil.asymCipher(plainBytes, publicKey);
		assertNotNull(secondcipheredArgument);
		
		System.out.print("Encoded text: ");
		String secondcipheredText = DatatypeConverter.printBase64Binary(secondcipheredArgument);
		System.out.println(secondcipheredText);
		System.out.print("Encoded text Bytes: ");
		System.out.println(secondcipheredArgument);

		// verify first ciphered with second ciphered
		assertEquals(decipheredArgument, secondcipheredArgument);
		assertEquals(decipheredText, secondcipheredText);
	}
	
	/**
	 * Generate a digital signature using the signature object provided by Java.
	 */
	@Test
	public void testSignature() throws Exception {
		System.out.print("TEST ");
		System.out.print(SIGNATURE_ALGORITHM);
		System.out.print(" digital signature");
		System.out.println(" with public key in X509 certificate and private key in JDK keystore:");

		System.out.print("Text: ");
		System.out.println(plainText);
		System.out.print("Bytes: ");
		System.out.println(printHexBinary(plainBytes));

		// make digital signature
		System.out.println("Signing ...");
		PrivateKey privateKey = CertUtil.getPrivateKeyFromKeyStoreResource(KEYSTORE,
				KEYSTORE_PASS.toCharArray(), KEY_ALIAS, KEY_PASS.toCharArray());
		byte[] digitalSignature = CertUtil.makeDigitalSignature(SIGNATURE_ALGORITHM, privateKey, plainBytes);
		assertNotNull(digitalSignature);

		// verify the signature
		System.out.println("Verifying ...");
		PublicKey publicKey = CertUtil.getX509CertificateFromResource(CERTIFICATE).getPublicKey();
		boolean result = CertUtil.verifyDigitalSignature(SIGNATURE_ALGORITHM, publicKey, plainBytes, digitalSignature);
		assertTrue(result);

		// data modification ...
		plainBytes[3] = 12;
		System.out.println("Tampered bytes: (look closely around the 7th hex character)");
		System.out.println(printHexBinary(plainBytes));
		System.out.println("      ^");

		// verify the signature
		System.out.println("Verifying ...");
		boolean resultAfterTamper = CertUtil.verifyDigitalSignature(SIGNATURE_ALGORITHM, publicKey, plainBytes,
				digitalSignature);
		assertFalse(resultAfterTamper);
	}

}
