package assignment2;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class Server {

	public static void main(String[] args) {

    	int port = 4321;
    	if (args.length > 0) port = Integer.parseInt(args[0]);

		ServerSocket welcomeSocket = null;
		Socket connectionSocket = null;
		DataOutputStream toClient = null;
		DataInputStream fromClient = null;

		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedFileOutputStream = null;

		try {
			welcomeSocket = new ServerSocket(port);
			connectionSocket = welcomeSocket.accept();
			fromClient = new DataInputStream(connectionSocket.getInputStream());
			toClient = new DataOutputStream(connectionSocket.getOutputStream());
			Key privateKey = null;

			while (!connectionSocket.isClosed()) {

				int packetType = fromClient.readInt();

				// If the packet is for transferring the filename
				if (packetType == 0) {

					System.out.println("Receiving file...");

					int numBytes = fromClient.readInt();
					byte [] filename = new byte[numBytes];
					// Must use read fully!
					// See: https://stackoverflow.com/questions/25897627/datainputstream-read-vs-datainputstream-readfully
					fromClient.readFully(filename, 0, numBytes);

					fileOutputStream = new FileOutputStream("recv_"+new String(filename, 0, numBytes));
					bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);

				// If the packet is for transferring a chunk of the file
				// for CP-1
				} else if (packetType == 1) {
					
					Cipher cipher = null;
					if (privateKey != null){
						cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
						cipher.init(Cipher.DECRYPT_MODE, privateKey);
					} else {
						cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
						cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
					}
					
					int numBytes = fromClient.readInt();
					byte [] block = new byte[128];
					fromClient.readFully(block, 0, 128);
					block = cipher.doFinal(block);
					
					// decrypt the block and add it to the file stream.
					if (numBytes > 0){
						bufferedFileOutputStream.write(block, 0, numBytes);
					}
					
					if (numBytes < 117) {
						System.out.println("Closing connection...");

						if (bufferedFileOutputStream != null) bufferedFileOutputStream.close();
						if (bufferedFileOutputStream != null) fileOutputStream.close();
						fromClient.close();
						toClient.close();
						connectionSocket.close();
					}
				} else if (packetType == 2){
					
					// for sending the server's signed certificate
					File f = new File("server.crt");
					long byteLength = f.length();
					byte[] file = new byte[(int) byteLength];
					toClient.writeLong(byteLength);
					InputStream fileStream = new FileInputStream(f);
					fileStream.read(file);
					toClient.write(file);
					fileStream.close();
					
				} else if (packetType == 3){
					
					// ask for the shared challenge message to be encrypted.
					Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
					cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey());
					byte[] encrypted = cipher.doFinal(Client.CHALLENGE.getBytes());
					toClient.writeInt(encrypted.length);
					toClient.write(encrypted);
				} else if (packetType == 4) {
					// establish a shared session key
					System.out.println("establishing shared session key..");
					byte[] shared = new byte[128];
					fromClient.readFully(shared, 0, shared.length);
					Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
					cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
					privateKey = new SecretKeySpec(cipher.doFinal(shared), "AES");
				}

			}
		} catch (Exception e) {e.printStackTrace();}

	}
	
	private static Key getPrivateKey() throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeySpecException{
		File f = new File("privateServer.der");
		long byteLength = f.length();
		byte[] file = new byte[(int) byteLength];
		InputStream fileStream = new FileInputStream(f);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		fileStream.read(file);
		
		KeySpec keySpec = new PKCS8EncodedKeySpec(file);
		PrivateKey key = kf.generatePrivate(keySpec);
		fileStream.close();
		
		return key;
	}

}
