package co.madteam.madmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Command {

	public static void line(String command) {
		Runtime runtime = Runtime.getRuntime();
		Process proc = null;
		OutputStreamWriter osw = null;
		try {
			proc = runtime.exec("su");
			osw = new OutputStreamWriter(proc.getOutputStream());
			osw.write(command);
			osw.flush();
			osw.close();
			proc.waitFor();

		} catch (InterruptedException e) {
		} catch (IOException e) {
		}
		return;
	}

	public static void s(String[] commands) {
		Runtime runtime = Runtime.getRuntime();
		Process proc = null;
		OutputStreamWriter osw = null;
		try {
			proc = runtime.exec("su");
			osw = new OutputStreamWriter(proc.getOutputStream());

			for (String cmd : commands) {
				osw.write(cmd + "\n");
			}
			osw.flush();
			osw.close();
			proc.waitFor();

		} catch (InterruptedException e) {
		} catch (IOException e) {
		}
		return;
	}

	public static boolean isSUed() {
		Runtime runtime = Runtime.getRuntime();
		Process proc = null;
		OutputStreamWriter osw = null;
		try {
			proc = runtime.exec("su");
			osw = new OutputStreamWriter(proc.getOutputStream());
			osw.write("id");
			osw.flush();
			osw.close();
			proc.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			int read;
			char[] buffer = new char[4096];
			StringBuffer output = new StringBuffer();
			try {
				while ((read = reader.read(buffer)) > 0) {
					output.append(buffer, 0, read);
				}
			} catch (IOException e) {
				return false;
			}
			String id = output.toString();
			if (id.contains("uid=0(root)")) {
				return true;

			}

		} catch (InterruptedException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return false;
	}

	public static boolean hasSU() {

		File su1 = new File("/system/bin/su");
		File su2 = new File("/system/xbin/su");
		if (su1.exists()) {
			return true;
		} else if (su2.exists()) {
			return true;
		}
		return false;
	}

	public static String b(String command) {
		Runtime runtime = Runtime.getRuntime();
		Process proc = null;
		try {
			proc = runtime.exec(command);
		} catch (IOException ex) {
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				proc.getInputStream()));
		int read;
		char[] buffer = new char[4096];
		StringBuffer output = new StringBuffer();
		try {
			while ((read = reader.read(buffer)) > 0) {
				output.append(buffer, 0, read);
			}
		} catch (IOException e) {
		}
		String exit = output.toString().trim();
		return exit;

	}

	public static void checkDir(File dir) {
		if (dir.exists()) {
			return;
		} else {
			dir.mkdirs();
			return;
		}
	}

	public static String appSign(Context c) {

		String str = c.getText(R.string.app_name) + " v"
				+ c.getText(R.string.version);
		return str;
	}


	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}
	
	public static void reboot(Context context, String param) {
		Assets.copyFile(context, "reboot");
		String reboot = "/data/data/co.madteam.madmanager/files/reboot";
		Command.line("chmod 777 " + reboot);
		if (param != null) {
			Command.line(reboot + " " + param);
		} else {
			Command.line(reboot);
		}
	}

}
