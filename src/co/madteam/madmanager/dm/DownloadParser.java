package co.madteam.madmanager.dm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class DownloadParser {

	private String mFilename;
	private String mURL;
	private String mFileSize;
	private String mFileID;

	public DownloadParser(String file) {

		String[] arrayOfString = new String[4];

		try {
			FileInputStream FileInputStream = new FileInputStream(file);
			DataInputStream dataInputStream = new DataInputStream(
					FileInputStream);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(dataInputStream));
			String readLine;
			int line = 0;
			while ((readLine = bufferedReader.readLine()) != null
					&& line <= arrayOfString.length) {
				arrayOfString[line] = readLine;
				line += 1;
			}
			dataInputStream.close();
		} catch (Exception e) {
		}

		mFilename = arrayOfString[0];
		mURL = arrayOfString[1];
		mFileSize = arrayOfString[2];
		mFileID = arrayOfString[3];

	}

	public String getFilename() {
		return mFilename;
	}

	public String getURL() {
		return mURL;
	}

	public long getFileSize() {
		long size = 0;
		try {
			size = Long.parseLong(mFileSize);
		} catch (NumberFormatException e) {
		}
		return size;
	}

	public int getID() {
		int id = -2;
		try {
			id = Integer.parseInt(mFileID);
		} catch (NumberFormatException e) {
		}
		return id;
	}

	public static void generateFile(String file, String filename, String url,
			String length, int id) {

		String br = "\n";
		StringBuilder info = new StringBuilder();
		info.append(filename).append(br);
		info.append(url).append(br);
		info.append(length).append(br);
		info.append(id);

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(info.toString());
			writer.close();

		} catch (IOException e) {
		}

	}
}
