package org.komparator.security;

import java.security.*;
import javax.crypto.*;
import javax.xml.bind.DatatypeConverter;

import pt.ulisboa.tecnico.sdis.cert.CertUtil;


public class CryptoUtil {
	
	private static final String PADDING = "RSA/ECB/PKCS1Padding";
	private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
	
	public static byte[] asymCipher(byte[] plainBytes, Key publicKey) {
		System.out.println("original string: " + DatatypeConverter.printBase64Binary(plainBytes));
        byte[] cipherBytes = transform(Cipher.ENCRYPT_MODE, plainBytes, publicKey);
		System.out.println("encrypted string: " + DatatypeConverter.printBase64Binary(cipherBytes));
		return cipherBytes;
	}
	
	public static byte[] asymDecipher(byte[] cipherBytes, Key privateKey) {
		System.out.println("encrypted string: " + DatatypeConverter.printBase64Binary(cipherBytes));
        byte[] plainBytes = transform(Cipher.DECRYPT_MODE, cipherBytes, privateKey);
		System.out.println("original string: " + DatatypeConverter.printBase64Binary(plainBytes));
		return plainBytes;
	}
	
	private static byte[] transform (int mode, byte[] data, Key key){
		byte [] transformedData = null;
        
		try {
			Cipher cipher = Cipher.getInstance(PADDING);
			
	        cipher.init(mode, key);

	        transformedData = cipher.doFinal(data);
	        
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			System.out.println("Cryptographic algorithm ou Padding mechanism requested not available.");
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			System.out.println("Invalid Key.");
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			System.out.println("Length of data provided to a block cipher incorrect.");
			e.printStackTrace();
		} catch (BadPaddingException e) {
			System.out.println("Data not padded properly.");
			e.printStackTrace();
		}

		return transformedData;
	}
	
	public static byte[] makeDigitalSignature(byte[] bytes, PrivateKey privateKey) throws Exception {
		// sign the plain-text with the private key
		
		return CertUtil.makeDigitalSignature(SIGNATURE_ALGORITHM, privateKey, bytes);
	}

	public static boolean verifyDigitalSignature(byte[] cipherDigest, byte[] bytes, PublicKey publicKey)
			throws Exception {
		// verify the signature with the public key
		Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
		sig.initVerify(publicKey);
		sig.update(bytes);
		
		try {
			return sig.verify(cipherDigest);
		} catch (SignatureException se) {
			System.out.println("Caught exception while verifying signature " + se);
			return false;
		}
	}	
}
