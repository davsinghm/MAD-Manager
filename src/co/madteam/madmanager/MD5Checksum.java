package co.madteam.madmanager;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import co.madteam.madmanager.utilities.MadUtils;

public class MD5Checksum {

	private static byte[] createChecksum(String filename) throws Exception {
		InputStream fis = new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();
		return complete.digest();
	}

	public static String getMD5Checksum(String filename) {
		byte[] b = null;
		try {
			b = createChecksum(filename);

		} catch (Exception e) {
			e.printStackTrace();
		}
		String result = "";
		if (b != null) {

			for (int i = 0; i < b.length; i++) {
				result += Integer.toString((b[i] & 0xff) + 0x100, 16)
						.substring(1);
			}
		}
		return result;

	}

	public static boolean verifyMD5Checksum(String file, String md5) {

		if (!MadUtils.isEmpty(md5)) {
			String md5nospace = md5.trim();
			String getMD5 = MD5Checksum.getMD5Checksum(file);
		
			if (getMD5.equalsIgnoreCase(md5nospace)) {
				return true;
			}
		}
		return false;	
	}

}
