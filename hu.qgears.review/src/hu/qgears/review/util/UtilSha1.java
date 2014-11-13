package hu.qgears.review.util;

import hu.qgears.commons.UtilFile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UtilSha1 {
	/**
	 * Get the SHA1 hash of the byte array. The hash is returned in a string
	 * containing a hexadecimal number
	 * 
	 * @param bytes
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String getSHA1(byte[] bytes) {
		try {
			MessageDigest m = MessageDigest.getInstance("SHA1");
			m.update(bytes);
			return "" + new BigInteger(1, m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * Get the SHA1 hash of the byte array. The hash is returned in a string
	 * containing a hexadecimal number
	 * 
	 * @param f file that is loaded and its content us hashed.
	 * @return
	 * @throws NoSuchAlgorithmException, {@link IOException}
	 */
	public static String getSHA1(File f) throws IOException {
		return getSHA1(UtilFile.loadFile(f));
	}
}
