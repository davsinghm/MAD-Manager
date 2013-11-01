package co.madteam.madmanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;

public class Assets {

	public static void copyFile(Context context, String filename) {
		AssetManager assetManager = context.getAssets();

		String destination = "/data/data/" + context.getPackageName()
				+ "/files/";

		File filedir = new File(destination);
		if (!filedir.exists()) {
			filedir.mkdir();
		}

		try {
			InputStream in = assetManager.open(filename);
			String newFileName = destination + filename;
			OutputStream out = new FileOutputStream(newFileName);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			out.flush();
			out.close();
		} catch (Exception e) {
		}

	}
}
