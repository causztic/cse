package assignment2;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.xml.bind.DatatypeConverter;

public class Client {
	static final String CHALLENGE = "HotDogHeroesAreDeliciousAndJumpy";
	private static byte[] challengeResponse = new byte[0];
	private static PublicKey serverKey;
	
	public static void main(String[] args) {

		String filename = "5mb";
		if (args.length > 0)
			filename = args[0];

		String serverAddress = "localhost";
		if (args.length > 1)
			serverAddress = args[1];

		int port = 4321;
		if (args.length > 2)
			port = Integer.parseInt(args[2]);
		
		int mode = 2;
		if (args.length > 3)
			mode = Integer.parseInt(args[3]);

		int numBytes = 0;

		Socket clientSocket = null;

		DataOutputStream toServer = null;
		DataInputStream fromServer = null;

		FileInputStream fileInputStream = null;
		BufferedInputStream bufferedFileInputStream = null;

		long timeStarted = System.nanoTime();

		try {

			System.out.println("Establishing connection to server...");

			// Connect to server and get the input and output streams
			clientSocket = new Socket(serverAddress, port);
			toServer = new DataOutputStream(clientSocket.getOutputStream());
			fromServer = new DataInputStream(clientSocket.getInputStream());
			challengeResponse = getChallenge(toServer, fromServer);
			if (proveIdentity(toServer, fromServer)) {
				System.out.println("Identity verified.");
				System.out.println("Sending file...");
				
				// Send the filename
				toServer.writeInt(0);
				toServer.writeInt(filename.getBytes().length);
				toServer.write(filename.getBytes());
				// toServer.flush();

				// Open the file
				fileInputStream = new FileInputStream(filename);
				bufferedFileInputStream = new BufferedInputStream(fileInputStream);

				byte[] fromFileBuffer = new byte[117];
				byte[] encryptedBlock = new byte[0];
				Cipher cipher = null;
				
				if (mode == 1){
					// CP-1
					cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
					cipher.init(Cipher.ENCRYPT_MODE, serverKey);

				} else {
					// CP-2
					
					// Establish a shared session key for sharing
					KeyGenerator keyGen = KeyGenerator.getInstance("AES");
					keyGen.init(128);
					SecretKey secretKey = keyGen.generateKey();
					cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
					 // encrypt the shared session key with the server's public key
					cipher.init(Cipher.ENCRYPT_MODE, serverKey);
					
					toServer.writeInt(4);
					toServer.write(cipher.doFinal(secretKey.getEncoded()));
					
					cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
					cipher.init(Cipher.ENCRYPT_MODE, secretKey);
					
				}
				
				// Send the file
				for (boolean fileEnded = false; !fileEnded;) {
					numBytes = bufferedFileInputStream.read(fromFileBuffer);
					// encrypt with their public key
					encryptedBlock = cipher.doFinal(fromFileBuffer);
					fileEnded = numBytes < 117;

					toServer.writeInt(1);
					toServer.writeInt(numBytes);
					toServer.write(encryptedBlock); //fromFileBuffer);
					toServer.flush();
				}

			} else {
				System.err.println("Unable to verify server.");
			}

			bufferedFileInputStream.close();
			fileInputStream.close();

			System.out.println("Closing connection...");

		} catch (Exception e) {
			e.printStackTrace();
		}

		long timeTaken = System.nanoTime() - timeStarted;
		System.out.println("Program took: " + timeTaken / 1000000.0 + "ms to run");
	}
	private static byte[] getChallenge(DataOutputStream toServer, DataInputStream fromServer) throws IOException {
		toServer.writeInt(3);
		byte[] bytes = new byte[fromServer.readInt()];
		fromServer.readFully(bytes);
		return bytes;
	}
	private static boolean proveIdentity(DataOutputStream toServer, DataInputStream fromServer) throws IOException {
		try {
			InputStream fis = new FileInputStream("CA.crt");
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate CAcert = (X509Certificate) cf.generateCertificate(fis);
			CAcert.checkValidity(); // check if CA's cert is valid

			// request from the server their certificate.
			toServer.writeInt(2);
			long certSize = fromServer.readLong();
			byte[] serverCert = new byte[(int) certSize];
			fromServer.readFully(serverCert);

			InputStream serverCertStream = new ByteArrayInputStream(serverCert);
			X509Certificate ServerCert = (X509Certificate) cf.generateCertificate(serverCertStream);
			ServerCert.checkValidity();
			ServerCert.verify(CAcert.getPublicKey());
			
			System.out.println("Certificate valid, verifying challenge..");
			serverKey = ServerCert.getPublicKey();
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, serverKey);
			byte[] decrypted = cipher.doFinal(challengeResponse);
			return new String(decrypted).equals(CHALLENGE);

		} catch (CertificateException e) {
			System.err.println("Invalid certificate.");
		} catch (SignatureException e) {
			System.err.println("Server used a certificate that does not correspond to CA.");
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
