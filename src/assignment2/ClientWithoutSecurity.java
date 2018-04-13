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
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class ClientWithoutSecurity {

	public static void main(String[] args) {

		String filename = "cat-1.txt";
		if (args.length > 0)
			filename = args[0];

		String serverAddress = "localhost";
		if (args.length > 1)
			filename = args[1];

		int port = 4321;
		if (args.length > 2)
			port = Integer.parseInt(args[2]);

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

				// Send the file
				for (boolean fileEnded = false; !fileEnded;) {
					numBytes = bufferedFileInputStream.read(fromFileBuffer);
					fileEnded = numBytes < 117;

					toServer.writeInt(1);
					toServer.writeInt(numBytes);
					toServer.write(fromFileBuffer);
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
			return true;

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
		}
		return false;
	}
}
