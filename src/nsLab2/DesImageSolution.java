package nsLab2;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.crypto.*;
import javax.imageio.ImageIO;

public class DesImageSolution {
	public static void main(String[] args) throws Exception {
		int image_width = 200;
		int image_length = 200;
		// read image file and save pixel value into int[][] imageArray
		BufferedImage img = ImageIO.read(new File("traingle.bmp"));
		image_width = img.getWidth();
		image_length = img.getHeight();
		int[][] imageArray = new int[image_width][image_length];
		for (int idx = 0; idx < image_width; idx++) {
			for (int idy = 0; idy < image_length; idy++) {
				int color = img.getRGB(idx, idy);
				imageArray[idx][idy] = color;
			}
		}
		// TODO: generate secret key using DES algorithm
		SecretKey key = KeyGenerator.getInstance("DES").generateKey();
		// TODO: Create cipher object, initialize the ciphers with the given
		// key, choose encryption algorithm/mode/padding,
		// you need to try both ECB and CBC mode, use PKCS5Padding padding
		// method
		
		// Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding"); CBC is better because ECB still has outline
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);

		// define output BufferedImage, set size and format
		BufferedImage outImage = new BufferedImage(image_width, image_length, BufferedImage.TYPE_3BYTE_BGR);

		for (int idx = 0; idx < image_width; idx++) {
			// convert each column int[] into a byte[] (each_width_pixel)
			byte[] each_width_pixel = new byte[4 * image_length];
			for (int idy = 0; idy < image_length; idy++) {
				ByteBuffer dbuf = ByteBuffer.allocate(4);
				dbuf.putInt(imageArray[idx][idy]);
				byte[] bytes = dbuf.array();
				System.arraycopy(bytes, 0, each_width_pixel, idy * 4, 4);
			}
			// TODO: encrypt each column or row bytes
			byte[] output = cipher.doFinal(each_width_pixel);
			// TODO: convert the encrypted byte[] back into int[] and write to
			IntBuffer intBuf = ByteBuffer.wrap(output).asIntBuffer();
			int[] array = new int[intBuf.remaining()];
			intBuf.get(array);
			// outImage (use setRGB)
			for (int i = 0; i < image_length; i++){
				outImage.setRGB(idx, i, array[i]);
			}

		}
		// write outImage into file
		ImageIO.write(outImage, "BMP", new File("CBC_triangle.bmp"));
	}
}