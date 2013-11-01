package co.madteam.madmanager.utilities;

import java.io.File;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;

public class MadEnvironment {

	public static String getDefaultBackupPath(Context c) {
		String str = "/sdcard";
		boolean isExtStoragePrefered = PreferenceManager
				.getDefaultSharedPreferences(c).getBoolean(
						"use_external_storage", true);
		if (isExternalSDCardMounted() && isExtStoragePrefered) {
			str = "/emmc";
		}
		return str;
	}

	public static boolean isExternalSDCardMounted() {

		if (getExternalSDCardPath() == null) {
			return false;
		} else {
			
			File extSdCard = new File(getExternalSDCardPath());
			File sdCard = new File(Environment.getExternalStorageDirectory()
					.getPath());
			if (extSdCard.exists() && sdCard.exists()) {
				StatFs extSdCardFs = new StatFs(extSdCard.getAbsolutePath());
				StatFs sdCardFs = new StatFs(sdCard.getAbsolutePath());
				long extBlocks = extSdCardFs.getAvailableBlocks();
				long intBlocks = sdCardFs.getAvailableBlocks();

				if (extBlocks != intBlocks && extBlocks != 0) {
					return true;
				}
			}
		}
		return false;
	}

	public static String getExternalSDCardPath() {

		String sdCardPath = Environment.getExternalStorageDirectory().getPath();

		String[] extPaths = new String[7];
		extPaths[0] = sdCardPath + "/external_sd";
		extPaths[1] = sdCardPath + "/sd";
		extPaths[2] = sdCardPath + "/_ExternalSD";
		extPaths[3] = "/mnt/extSdCard";
		extPaths[4] = "/mnt/sdcard-ext";
		extPaths[5] = "/mnt/extsd";
		extPaths[6] = "/mnt/extSd";

		for (String path : extPaths) {
			File dir = new File(path);
			if (dir.exists() && dir.isDirectory()) {
				return dir.getAbsolutePath();
			}

		}
		return null;
	}

	/*
	 * public static String getExternalSDCardPath() {
	 * 
	 * File file = null;
	 * 
	 * String path = Environment.getExternalStorageDirectory().getPath(); String
	 * extendedPath = "";
	 * 
	 * if (Build.DEVICE.toLowerCase().contains("samsung") ||
	 * Build.MANUFACTURER.toLowerCase().contains("samsung")) { extendedPath =
	 * "/external_sd"; try { file = new File(path + extendedPath); if
	 * (file.exists() && file.isDirectory()) { return file.getAbsolutePath(); }
	 * else if (Build.MODEL.toLowerCase().contains("gt-i9300") ||
	 * Build.MODEL.toLowerCase().contains("gt-s7562")) { extendedPath =
	 * "/mnt/extSdCard"; try { file = new File(extendedPath); if (file.exists()
	 * && file.isDirectory()) { return file.getAbsolutePath(); } } catch
	 * (Exception e) { } } else { extendedPath = "/sd"; } } catch (Exception e)
	 * { } } else if (Build.DEVICE.toLowerCase().contains("e0") ||
	 * Build.MANUFACTURER.toLowerCase().contains("LGE")) { extendedPath =
	 * "/_ExternalSD"; } else if
	 * (Build.MANUFACTURER.toLowerCase().contains("motorola") ||
	 * Build.DEVICE.toLowerCase().contains("olympus")) { file = new
	 * File("/mnt/sdcard-ext"); } try { if (!extendedPath.equals("")) { file =
	 * new File(path + extendedPath); } if (file.exists() && file.isDirectory())
	 * { return file.getAbsolutePath(); } } catch (Exception e) {
	 * 
	 * return "/mnt/extsd"; }
	 * 
	 * return "/mnt/extsd"; }
	 */

	public static boolean isUSBStorageExist() {
		if (getExternalSDCardPath() != null) {
			return true;
		}
		return false;
	}
}
