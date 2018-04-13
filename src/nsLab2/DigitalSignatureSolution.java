package nsLab2;

import javax.xml.bind.DatatypeConverter;
import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.security.*;

public class DigitalSignatureSolution {

	public static void main(String[] args) throws Exception {
		// Read the text file and save to String data
		String fileName = "smallSize.txt";
		String data = "";
		String line;
		BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
		while ((line = bufferedReader.readLine()) != null) {
			data = data + "\n" + line;
		}
		bufferedReader.close();
		
		System.out.println("Original content: " + data);

		// TODO: generate a RSA keypair, initialize as 1024 bits, get public key
		// and private key from this keypair.
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		keygen.initialize(1024);
		KeyPair kp = keygen.generateKeyPair();
		Key publicKey = kp.getPublic();
		Key privateKey = kp.getPrivate();
		
		// TODO: Calculate message digest, using MD5 hash function
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] digest = md.digest(data.getBytes());

		// TODO: print the length of output digest byte[], compare the length of
		// file smallSize.txt and largeSize.txt
		System.out.println("digest has length of " + digest.length);
		System.out.println("file read has length of " + data.length());

		// TODO: Create RSA("RSA/ECB/PKCS1Padding") cipher object and initialize
		// is as encrypt mode, use PRIVATE key.
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);

		// TODO: encrypt digest message
		byte[] encryptedDigest = cipher.doFinal(digest);

		// TODO: print the encrypted message (in base64format String using
		// DatatypeConverter)
		String encryptedString = DatatypeConverter.printBase64Binary(encryptedDigest);
		System.out.println("Encrypted: " + encryptedDigest.length + ": " + encryptedString);
		// TODO: Create RSA("RSA/ECB/PKCS1Padding") cipher object and initialize
		// is as decrypt mode, use PUBLIC key.
		cipher.init(Cipher.DECRYPT_MODE, publicKey);

		// TODO: decrypt message
		byte[] decryptedDigest = cipher.doFinal(encryptedDigest);

		// TODO: print the decrypted message (in base64format String using
		// DatatypeConverter), compare with origin digest
		String decrypted = DatatypeConverter.printBase64Binary(decryptedDigest);
		String digestString = DatatypeConverter.printBase64Binary(digest);
		System.out.println("Decrypted: " + decrypted);
		System.out.println("Original: " + digestString);
		System.out.println("Same? " + decrypted.equals(digestString));
	}

}
