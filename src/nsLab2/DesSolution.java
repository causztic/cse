package nsLab2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.crypto.*;
import javax.xml.bind.DatatypeConverter;

public class DesSolution {
	public static void main(String[] args) throws Exception {
		String fileName = "largeSize.txt";
		String data = "";
		String line;
		BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
		while ((line = bufferedReader.readLine()) != null) {
			data = data + "\n" + line;
		}
		bufferedReader.close();
		System.out.println("Original content: " + data);

		// TODO: generate secret key using DES algorithm
		SecretKey key = KeyGenerator.getInstance("DES").generateKey();

		// TODO: create cipher object, initialize the ciphers with the given
		// key, choose encryption mode as DES
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		// TODO: do encryption, by calling method Cipher.doFinal().
		byte[] encrypted = cipher.doFinal(data.getBytes());
//		System.out.println(new String(encrypted));
		// TODO: print the length of output encrypted byte[], compare the length
		// of file smallSize.txt and largeSize.txt
		System.out.println("Encrypted length: " + encrypted.length);
		System.out.println("file read has length of " + data.length());

		// TODO: do format conversion. Turn the encrypted byte[] format into
		// base64format String using DatatypeConverter
		String base64format = DatatypeConverter.printBase64Binary(encrypted);
		
		// TODO: print the encrypted message (in base64format String format)
		System.out.println(base64format);

		// TODO: create cipher object, initialize the ciphers with the given
		// key, choose decryption mode as DES
		cipher.init(Cipher.DECRYPT_MODE, key);

		// TODO: do decryption, by calling method Cipher.doFinal().
		byte[] decrypted = cipher.doFinal(encrypted);

		// TODO: do format conversion. Convert the decrypted byte[] to String,
		// using "String a = new String(byte_array);"
		String decryptedString = new String(decrypted);

		// TODO: print the decrypted String text and compare it with original
		// text
		//System.out.println("decrypted: " + decryptedString);
		//System.out.println("original: " + data);
		System.out.println("same? :" + decryptedString.equals(data));
	}
}