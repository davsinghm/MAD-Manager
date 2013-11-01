package co.madteam.madmanager.dm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import co.madteam.madmanager.MD5Checksum;
import co.madteam.madmanager.NewDownload;
import co.madteam.madmanager.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class DownloadServiceOLDOLD extends Service {

	public static String mLink;
	public static String mFilename;

	public static String mMD5 = "";
	static long mTotal = 1;

	static long mDownloaded_part1 = 0;
	static long mDownloaded_part2 = 0;
	static long mDownloaded_part3 = 0;
	static long mDownloaded_part4 = 0;
	static long mDownloaded_part5 = 0;
	static long mDownloaded_part6 = 0;
	static long mDownloaded_part7 = 0;
	static long mDownloaded_part8 = 0;

	static boolean isDownloading_part1 = false;
	static boolean isDownloading_part2 = false;
	static boolean isDownloading_part3 = false;
	static boolean isDownloading_part4 = false;
	static boolean isDownloading_part5 = false;
	static boolean isDownloading_part6 = false;
	static boolean isDownloading_part7 = false;
	static boolean isDownloading_part8 = false;

	static int mTotal_part = 1;
	static int mParts = 1;

	private AsyncTask<String, String, String> startDownload;
	private static AsyncTask<Integer, Integer, String> part1;
	private static AsyncTask<Integer, Integer, String> part2;
	private static AsyncTask<Integer, Integer, String> part3;
	private static AsyncTask<Integer, Integer, String> part4;
	private static AsyncTask<Integer, Integer, String> part5;
	private static AsyncTask<Integer, Integer, String> part6;
	private static AsyncTask<Integer, Integer, String> part7;
	private static AsyncTask<Integer, Integer, String> part8;

	public static String mFilesize_T;
	public static String mStatus_T;

	public static String mDownloaded_part1_info_T;
	public static String mDownloaded_part2_info_T;
	public static String mDownloaded_part3_info_T;
	public static String mDownloaded_part4_info_T;
	public static String mDownloaded_part5_info_T;
	public static String mDownloaded_part6_info_T;
	public static String mDownloaded_part7_info_T;
	public static String mDownloaded_part8_info_T;

	public static String mDownloaded_part1_T;
	public static String mDownloaded_part2_T;
	public static String mDownloaded_part3_T;
	public static String mDownloaded_part4_T;
	public static String mDownloaded_part5_T;
	public static String mDownloaded_part6_T;
	public static String mDownloaded_part7_T;
	public static String mDownloaded_part8_T;

	public static int mProgressBar1_int;
	public static int mProgressBar2_int;
	public static int mProgressBar3_int;
	public static int mProgressBar4_int;
	public static int mProgressBar5_int;
	public static int mProgressBar6_int;
	public static int mProgressBar7_int;
	public static int mProgressBar8_int;

	public static boolean isJoining;
	public static boolean isJoined;

	public static Bitmap mRomIcon = null;

	private Notification mNotification;
	private NotificationManager mNotificationManager;

	private static Context mContext;

	private Handler mHandler;
	public static boolean cancelled;

	private int last_progress = 0;

	int mID = 555;

	private Runnable mUpdateNoti = new Runnable() {
		public void run() {

			int Progress = (int) (mDownloaded_long() * 100 / mTotal);

			if (Progress != last_progress) {
				mNotification.contentView.setTextViewText(R.id.noti_info,
						Progress + "%");
				mNotification.contentView.setProgressBar(R.id.noti_progress,
						100, Progress, false);
				mNotificationManager.notify(mID, mNotification);

			}
			last_progress = Progress;

			if (!DownloadServiceOLD.isCompleted_Really() && !cancelled) {

				mHandler.postDelayed(this, 2000);
			} else if (DownloadServiceOLD.isCompleted_Really()) {

				stopForeground(true);

				PendingIntent pi = PendingIntent.getActivity(mContext, 0,
						new Intent(), 0);

				Notification notification = new Notification(
						R.drawable.ic_noti_download, null,
						System.currentTimeMillis());
				notification.setLatestEventInfo(mContext, "Download Complete!",
						mFilename, pi);

				mNotificationManager.notify(mID, notification);

				stopSelf();

			} else if (cancelled) {
				stopForeground(true);

				PendingIntent pi = PendingIntent.getActivity(mContext, 0,
						new Intent(), 0);

				Notification notification = new Notification(
						R.drawable.ic_noti_download, null,
						System.currentTimeMillis());
				notification.setLatestEventInfo(mContext,
						"Download Interrupted!", mFilename, pi);

				mNotificationManager.notify(mID, notification);

				stopSelf();
			}

			else if (mStatus_T.equals("Error")) {
				stopForeground(true);

				PendingIntent pi = PendingIntent.getActivity(mContext, 0,
						new Intent(), 0);

				Notification notification = new Notification(
						R.drawable.ic_noti_download, null,
						System.currentTimeMillis());
				notification.setLatestEventInfo(mContext, "Download Error",
						mFilename, pi);

				mNotificationManager.notify(mID, notification);

				stopSelf();
			}

		}
	};

	@Override
	public void onCreate() {
		super.onCreate();

		if (!NewDownload.isDownloadExecuted) {
			stopSelf();
			return;
		}
		mContext = getApplicationContext();

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		mParts = sharedPreferences.getInt("connections", 4);
		if (mLink.contains("gigashare.")) {
			mParts = 1;
		}

		isJoining = false;
		isJoined = false;
		cancelled = false;

		if (mParts == 1) {
			isDownloading_part1 = true;
		}

		if (mParts == 2) {
			isDownloading_part1 = true;
			isDownloading_part2 = true;
		}

		if (mParts == 4) {
			isDownloading_part1 = true;
			isDownloading_part2 = true;
			isDownloading_part3 = true;
			isDownloading_part4 = true;
		}
		if (mParts == 8) {
			isDownloading_part1 = true;
			isDownloading_part2 = true;
			isDownloading_part3 = true;
			isDownloading_part4 = true;
			isDownloading_part5 = true;
			isDownloading_part6 = true;
			isDownloading_part7 = true;
			isDownloading_part8 = true;

		}

		File dir = new File("/sdcard/madmanager/temp/");
		if (!dir.exists()) {
			dir.mkdirs();
		}

		Intent i = new Intent(mContext, DownloadActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pi = PendingIntent.getActivity(mContext, 0, i, 0);

		mNotificationManager = (NotificationManager) mContext
				.getSystemService(NOTIFICATION_SERVICE);

		mNotification = new Notification(R.drawable.ic_noti_download, null,
				System.currentTimeMillis());

		mNotification.flags = Notification.FLAG_ONGOING_EVENT;
		mNotification.contentView = new RemoteViews(mContext.getPackageName(),
				R.layout.noti_download);
		mNotification.contentIntent = pi;
		mNotification.contentView.setTextViewText(R.id.noti_title,
				"Downloading...");

		mNotification.contentView.setTextViewText(R.id.noti_text, mFilename);

		if (mRomIcon != null) {
			mNotification.contentView.setImageViewBitmap(R.id.noti_icon,
					mRomIcon);
		} else {
			mNotification.contentView.setImageViewResource(R.id.noti_icon,
					R.drawable.ic_launcher);
		}

		mNotification.contentView.setProgressBar(R.id.noti_progress, 100, 0,
				true);

		mNotificationManager.notify(mID, mNotification);

		startForeground(mID, mNotification);

		mHandler = new Handler();
		mHandler.post(mUpdateNoti);

		startDownload = new AsyncTask<String, String, String>() {

			@Override
			protected void onPreExecute() {

				mStatus_T = "Connecting...";
			}

			@Override
			protected String doInBackground(String... arg) {

				try {

					URL url = new URL(mLink);

					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();

					long Total = connection.getContentLength();
					if (Total == 0) {
						return ("Error");
					} else {
						mTotal = Total;
					}

					mTotal_part = (int) mTotal / mParts;
					publishProgress();

				} catch (Exception e) {
					return ("Error: " + e);
				}
				return "success";

			}

			@Override
			protected void onProgressUpdate(String... p) {

				String unit = " Bytes";

				double total = mTotal;
				double size = total;

				if (size > 1024) {
					size = total / 1024;
					unit = " KB";
				}
				if (size > 1024) {
					size = total / 1024 / 1024;
					unit = " MB";
				}
				DecimalFormat decimalFormat = new DecimalFormat("#.##");

				mFilesize_T = decimalFormat.format(size) + unit;

			}

			@Override
			protected void onPostExecute(String result) {

				if (result.contains("Error")) {
					return;
				}

				if (mParts == 8) {
					int part1_start = 0;
					int part1_end = mTotal_part - 1;

					part1.execute(part1_start, part1_end);

					int part2_start = mTotal_part;
					int part2_end = mTotal_part * 2 - 1;

					part2.execute(part2_start, part2_end);

					int part3_start = mTotal_part * 2;
					int part3_end = mTotal_part * 3 - 1;

					part3.execute(part3_start, part3_end);

					int part4_start = mTotal_part * 3;
					int part4_end = mTotal_part * 4 - 1;

					part4.execute(part4_start, part4_end);

					int part5_start = mTotal_part * 4;
					int part5_end = mTotal_part * 5 - 1;

					part5.execute(part5_start, part5_end);

					int part6_start = mTotal_part * 5;
					int part6_end = mTotal_part * 6 - 1;

					part6.execute(part6_start, part6_end);

					int part7_start = mTotal_part * 6;
					int part7_end = mTotal_part * 7 - 1;

					part7.execute(part7_start, part7_end);

					int part8_start = mTotal_part * 7;

					part8.execute(part8_start);

				}

				if (mParts == 4) {
					int part1_start = 0;
					int part1_end = mTotal_part - 1;

					part1.execute(part1_start, part1_end);

					int part2_start = mTotal_part;
					int part2_end = mTotal_part * 2 - 1;

					part2.execute(part2_start, part2_end);

					int part3_start = mTotal_part * 2;
					int part3_end = mTotal_part * 3 - 1;

					part3.execute(part3_start, part3_end);

					int part4_start = mTotal_part * 3;

					part4.execute(part4_start, 0);

				}

				if (mParts == 2) {
					int part1_start = 0;
					int part1_end = mTotal_part - 1;

					part1.execute(part1_start, part1_end);

					int part2_start = mTotal_part;

					part2.execute(part2_start, 0);

				}
				if (mParts == 1) {
					int part1_start = 0;

					part1.execute(part1_start, 0);

				}

				mStatus_T = "Receiving data...";

			}

		};
		startDownload.execute();

		// TODO PART1
		part1 = new AsyncTask<Integer, Integer, String>() {

			File file;
			FileOutputStream fos;

			@Override
			protected void onCancelled() {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}

				}
				super.cancel(true);
				mDownloaded_part1_info_T = "Disconnect";

			}

			@Override
			protected void onPreExecute() {

				mDownloaded_part1_info_T = "Sending GET...";
			}

			@Override
			protected String doInBackground(Integer... arg) {

				int start = arg[0];
				int end = arg[1];
				int downloaded = 0;

				file = new File("/sdcard/madmanager/temp/" + mFilename
						+ ".part1");

				try {

					URL url = new URL(mLink);

					if (file.length() == mTotal_part) {
						return "complete";
					}

					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();

					if (file.exists()) {
						downloaded = (int) file.length();
						start = (int) (arg[0] + file.length());
					}

					if (mParts == 1) {
						connection.setRequestProperty("Range", "bytes=" + start
								+ "-");
					} else {
						connection.setRequestProperty("Range", "bytes=" + start
								+ "-" + end);
					}

					connection.setDoInput(true);
					connection.setDoOutput(true);

					InputStream in = connection.getInputStream();

					fos = (downloaded == 0) ? new FileOutputStream(file)
							: new FileOutputStream(file, true);

					byte[] buffer = new byte[1024];
					int x = 0;

					while ((x = in.read(buffer)) >= 0) {
						fos.write(buffer, 0, x);
						downloaded += x;

						publishProgress(downloaded);

					}

					fos.close();

				} catch (Exception e) {

					return e + "";
				}

				return "complete";

			}

			@Override
			protected void onProgressUpdate(Integer... p) {

				mDownloaded_part1 = p[0];

				long progress1 = p[0];
				long progress = progress1 * 100 / mTotal_part;

				mProgressBar1_int = (int) progress;

				mDownloaded_part1_info_T = "Receiving data...";

				String unit = " Bytes";

				double downloaded = p[0];
				double size = downloaded;

				if (size > 1024) {
					size = downloaded / 1024;
					unit = " KB";
				}
				if (size > 1024) {
					size = downloaded / 1024 / 1024;
					unit = " MB";
				}
				DecimalFormat decimalFormat = new DecimalFormat("#.##");

				mDownloaded_part1_T = decimalFormat.format(size) + unit;

			}

			@Override
			protected void onPostExecute(String result) {

				if (result.equals("complete")) {
					mDownloaded_part1_info_T = "Complete";

					mProgressBar1_int = 100;

					mDownloaded_part1 = file.length();

					isDownloading_part1 = false;

				}

				else {
					mDownloaded_part1_info_T = "Disconnect";

				}

			}

		};

		// TODO PART2
		part2 = new AsyncTask<Integer, Integer, String>() {

			FileOutputStream fos;

			File file;

			@Override
			protected void onCancelled() {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}

				}
				super.cancel(true);
				mDownloaded_part2_info_T = "Disconnect";

			}

			@Override
			protected void onPreExecute() {

				mDownloaded_part2_info_T = "Sending GET...";

			}

			@Override
			protected String doInBackground(Integer... arg) {

				int start = arg[0];
				int end = arg[1];
				int downloaded = 0;
				file = new File("/sdcard/madmanager/temp/" + mFilename
						+ ".part2");

				try {

					URL url = new URL(mLink);

					if (mParts == 2) {
						int size1 = (int) mTotal;
						int size2 = size1 - mTotal_part;
						if (file.length() == size2) {
							return "complete";
						}
					} else if (file.length() == mTotal_part) {
						return "complete";
					}

					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();

					if (file.exists()) {
						downloaded = (int) file.length();
						start = (int) (arg[0] + file.length());
					}

					if (mParts == 2) {
						connection.setRequestProperty("Range", "bytes=" + start
								+ "-");
					} else {
						connection.setRequestProperty("Range", "bytes=" + start
								+ "-" + end);
					}

					connection.setDoInput(true);
					connection.setDoOutput(true);

					InputStream in = connection.getInputStream();

					fos = (downloaded == 0) ? new FileOutputStream(file)
							: new FileOutputStream(file, true);

					byte[] buffer = new byte[1024];
					int x = 0;

					while ((x = in.read(buffer)) >= 0) {
						fos.write(buffer, 0, x);
						downloaded += x;

						publishProgress(downloaded);

					}

					fos.close();

				} catch (Exception e) {

					return e + "";
				}

				return "complete";

			}

			@Override
			protected void onProgressUpdate(Integer... p) {

				mDownloaded_part2 = p[0];

				long progress1 = p[0];
				long progress = progress1 * 100 / mTotal_part;

				mProgressBar2_int = (int) progress;

				mDownloaded_part2_info_T = "Receiving data...";

				String unit = " Bytes";

				double downloaded = p[0];
				double size = downloaded;

				if (size > 1024) {
					size = downloaded / 1024;
					unit = " KB";
				}
				if (size > 1024) {
					size = downloaded / 1024 / 1024;
					unit = " MB";
				}
				DecimalFormat decimalFormat = new DecimalFormat("#.##");

				mDownloaded_part2_T = decimalFormat.format(size) + unit;

			}

			@Override
			protected void onPostExecute(String result) {

				if (result.equals("complete")) {
					mDownloaded_part2_info_T = "Complete";

					mProgressBar2_int = 100;

					mDownloaded_part2 = file.length();

					isDownloading_part2 = false;

				}

				else {
					mDownloaded_part2_info_T = "Disconnect";

				}

			}

		};

		// TODO PART3
		part3 = new AsyncTask<Integer, Integer, String>() {

			FileOutputStream fos;
			File file;

			@Override
			protected void onCancelled() {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}

				}
				super.cancel(true);
				mDownloaded_part3_info_T = "Disconnect";

			}

			@Override
			protected void onPreExecute() {
				mDownloaded_part3_info_T = "Sending GET...";
			}

			@Override
			protected String doInBackground(Integer... arg) {

				int start = arg[0];
				int end = arg[1];
				int downloaded = 0;
				file = new File("/sdcard/madmanager/temp/" + mFilename
						+ ".part3");

				try {

					URL url = new URL(mLink);

					if (file.length() == mTotal_part) {
						return "complete";
					}

					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();

					if (file.exists()) {
						downloaded = (int) file.length();
						start = (int) (arg[0] + file.length());
					}

					connection.setRequestProperty("Range", "bytes=" + start
							+ "-" + end);

					connection.setDoInput(true);
					connection.setDoOutput(true);

					InputStream in = connection.getInputStream();

					fos = (downloaded == 0) ? new FileOutputStream(file)
							: new FileOutputStream(file, true);

					byte[] buffer = new byte[1024];
					int x = 0;

					while ((x = in.read(buffer)) >= 0) {
						fos.write(buffer, 0, x);
						downloaded += x;

						publishProgress(downloaded);

					}

					fos.close();

				} catch (Exception e) {

					return e + "";
				}

				return "complete";

			}

			@Override
			protected void onProgressUpdate(Integer... p) {

				mDownloaded_part3 = p[0];

				long progress1 = p[0];
				long progress = progress1 * 100 / mTotal_part;

				mProgressBar3_int = (int) progress;

				mDownloaded_part3_info_T = "Receiving data...";

				String unit = " Bytes";

				double downloaded = p[0];
				double size = downloaded;

				if (size > 1024) {
					size = downloaded / 1024;
					unit = " KB";
				}
				if (size > 1024) {
					size = downloaded / 1024 / 1024;
					unit = " MB";
				}
				DecimalFormat decimalFormat = new DecimalFormat("#.##");

				mDownloaded_part3_T = decimalFormat.format(size) + unit;

			}

			@Override
			protected void onPostExecute(String result) {

				if (result.equals("complete")) {
					mDownloaded_part3_info_T = "Complete";

					mProgressBar3_int = 100;

					mDownloaded_part3 = file.length();

					isDownloading_part3 = false;

				}

				else {
					mDownloaded_part3_info_T = "Disconnect";

				}

			}

		};

		// TODO PART4
		part4 = new AsyncTask<Integer, Integer, String>() {

			FileOutputStream fos;
			File file;

			@Override
			protected void onCancelled() {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}

				}
				super.cancel(true);
				mDownloaded_part4_info_T = "Disconnect";

			}

			@Override
			protected void onPreExecute() {

				mDownloaded_part4_info_T = "Sending GET...";
			}

			@Override
			protected String doInBackground(Integer... arg) {

				int start = arg[0];
				int end = arg[1];
				int downloaded = 0;
				file = new File("/sdcard/madmanager/temp/" + mFilename
						+ ".part4");

				try {

					URL url = new URL(mLink);

					if (mParts == 4) {
						int parts = mParts - 1;
						int size1 = (int) mTotal;
						int size2 = size1 - (mTotal_part * parts);
						if (file.length() == size2) {
							return "complete";

						}
					} else if (file.length() == mTotal_part) {
						return "complete";
					}

					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();

					if (file.exists()) {
						downloaded = (int) file.length();
						start = (int) (arg[0] + file.length());
					}

					if (mParts == 4) {
						connection.setRequestProperty("Range", "bytes=" + start
								+ "-");
					} else {
						connection.setRequestProperty("Range", "bytes=" + start
								+ "-" + end);
					}

					connection.setDoInput(true);
					connection.setDoOutput(true);

					InputStream in = connection.getInputStream();

					fos = (downloaded == 0) ? new FileOutputStream(file)
							: new FileOutputStream(file, true);

					byte[] buffer = new byte[1024];
					int x = 0;

					while ((x = in.read(buffer)) >= 0) {
						fos.write(buffer, 0, x);
						downloaded += x;

						publishProgress(downloaded);

					}

					fos.close();

				} catch (Exception e) {

					return e + "";
				}

				return "complete";

			}

			@Override
			protected void onProgressUpdate(Integer... p) {

				mDownloaded_part4 = p[0];

				long progress1 = p[0];
				long progress = progress1 * 100 / mTotal_part;

				mProgressBar4_int = (int) progress;

				mDownloaded_part4_info_T = "Receiving data...";

				String unit = " Bytes";

				double downloaded = p[0];
				double size = downloaded;

				if (size > 1024) {
					size = downloaded / 1024;
					unit = " KB";
				}
				if (size > 1024) {
					size = downloaded / 1024 / 1024;
					unit = " MB";
				}
				DecimalFormat decimalFormat = new DecimalFormat("#.##");

				mDownloaded_part4_T = decimalFormat.format(size) + unit;

			}

			@Override
			protected void onPostExecute(String result) {

				if (result.equals("complete")) {
					mDownloaded_part4_info_T = "Complete";

					mProgressBar4_int = 100;

					mDownloaded_part4 = file.length();

					isDownloading_part4 = false;

				}

				else {
					mDownloaded_part4_info_T = "Disconnect";

				}

			}

		};

		// TODO PART5
		part5 = new AsyncTask<Integer, Integer, String>() {

			FileOutputStream fos;
			File file;

			@Override
			protected void onCancelled() {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}

				}
				super.cancel(true);
				mDownloaded_part5_info_T = "Disconnect";

			}

			@Override
			protected void onPreExecute() {

				mDownloaded_part5_info_T = "Sending GET...";
			}

			@Override
			protected String doInBackground(Integer... arg) {

				int start = arg[0];
				int end = arg[1];
				int downloaded = 0;
				file = new File("/sdcard/madmanager/temp/" + mFilename
						+ ".part5");

				try {

					URL url = new URL(mLink);

					if (file.length() == mTotal_part) {
						return "complete";
					}

					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();

					if (file.exists()) {
						downloaded = (int) file.length();
						start = (int) (arg[0] + file.length());
					}

					connection.setRequestProperty("Range", "bytes=" + start
							+ "-" + end);

					connection.setDoInput(true);
					connection.setDoOutput(true);

					InputStream in = connection.getInputStream();

					fos = (downloaded == 0) ? new FileOutputStream(file)
							: new FileOutputStream(file, true);

					byte[] buffer = new byte[1024];
					int x = 0;

					while ((x = in.read(buffer)) >= 0) {
						fos.write(buffer, 0, x);
						downloaded += x;

						publishProgress(downloaded);

					}

					fos.close();

				} catch (Exception e) {

					return e + "";
				}

				return "complete";

			}

			@Override
			protected void onProgressUpdate(Integer... p) {

				mDownloaded_part5 = p[0];

				long progress1 = p[0];
				long progress = progress1 * 100 / mTotal_part;

				mProgressBar5_int = (int) progress;

				mDownloaded_part5_info_T = "Receiving data...";

				String unit = " Bytes";

				double downloaded = p[0];
				double size = downloaded;

				if (size > 1024) {
					size = downloaded / 1024;
					unit = " KB";
				}
				if (size > 1024) {
					size = downloaded / 1024 / 1024;
					unit = " MB";
				}
				DecimalFormat decimalFormat = new DecimalFormat("#.##");

				mDownloaded_part5_T = decimalFormat.format(size) + unit;

			}

			@Override
			protected void onPostExecute(String result) {

				if (result.equals("complete")) {
					mDownloaded_part5_info_T = "Complete";

					mProgressBar5_int = 100;
					mDownloaded_part5 = file.length();

					isDownloading_part5 = false;

				}

				else {
					mDownloaded_part5_info_T = "Disconnect";

				}

			}

		};

		// TODO PART6
		part6 = new AsyncTask<Integer, Integer, String>() {

			FileOutputStream fos;

			File file;

			@Override
			protected void onCancelled() {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}

				}
				super.cancel(true);
				mDownloaded_part6_info_T = "Disconnect";

			}

			@Override
			protected void onPreExecute() {

				mDownloaded_part6_info_T = "Sending GET...";
			}

			@Override
			protected String doInBackground(Integer... arg) {

				int start = arg[0];
				int end = arg[1];
				int downloaded = 0;
				file = new File("/sdcard/madmanager/temp/" + mFilename
						+ ".part6");

				try {

					URL url = new URL(mLink);

					if (file.length() == mTotal_part) {
						return "complete";
					}

					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();

					if (file.exists()) {
						downloaded = (int) file.length();
						start = (int) (arg[0] + file.length());
					}

					connection.setRequestProperty("Range", "bytes=" + start
							+ "-" + end);
					connection.setDoInput(true);
					connection.setDoOutput(true);

					InputStream in = connection.getInputStream();

					fos = (downloaded == 0) ? new FileOutputStream(file)
							: new FileOutputStream(file, true);

					byte[] buffer = new byte[1024];
					int x = 0;

					while ((x = in.read(buffer)) >= 0) {
						fos.write(buffer, 0, x);
						downloaded += x;

						publishProgress(downloaded);

					}

					fos.close();

				} catch (Exception e) {

					return e + "";
				}

				return "complete";

			}

			@Override
			protected void onProgressUpdate(Integer... p) {

				mDownloaded_part6 = p[0];

				long progress1 = p[0];
				long progress = progress1 * 100 / mTotal_part;

				mProgressBar6_int = (int) progress;

				mDownloaded_part6_info_T = "Receiving data...";

				String unit = " Bytes";

				double downloaded = p[0];
				double size = downloaded;

				if (size > 1024) {
					size = downloaded / 1024;
					unit = " KB";
				}
				if (size > 1024) {
					size = downloaded / 1024 / 1024;
					unit = " MB";
				}
				DecimalFormat decimalFormat = new DecimalFormat("#.##");

				mDownloaded_part6_T = decimalFormat.format(size) + unit;

			}

			@Override
			protected void onPostExecute(String result) {

				if (result.equals("complete")) {
					mDownloaded_part6_info_T = "Complete";

					mProgressBar6_int = 100;

					mDownloaded_part6 = file.length();

					isDownloading_part6 = false;

				}

				else {
					mDownloaded_part6_info_T = "Disconnect";

				}

			}

		};

		// TODO PART7
		part7 = new AsyncTask<Integer, Integer, String>() {

			FileOutputStream fos;

			File file;

			@Override
			protected void onCancelled() {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}

				}
				super.cancel(true);
				mDownloaded_part7_info_T = "Disconnect";

			}

			@Override
			protected void onPreExecute() {

				mDownloaded_part7_info_T = "Sending GET...";
			}

			@Override
			protected String doInBackground(Integer... arg) {

				int start = arg[0];
				int end = arg[1];
				int downloaded = 0;
				file = new File("/sdcard/madmanager/temp/" + mFilename
						+ ".part7");

				try {

					URL url = new URL(mLink);

					if (file.length() == mTotal_part) {
						return "complete";
					}

					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();

					if (file.exists()) {
						downloaded = (int) file.length();
						start = (int) (arg[0] + file.length());
					}

					connection.setRequestProperty("Range", "bytes=" + start
							+ "-" + end);

					connection.setDoInput(true);
					connection.setDoOutput(true);

					InputStream in = connection.getInputStream();

					fos = (downloaded == 0) ? new FileOutputStream(file)
							: new FileOutputStream(file, true);

					byte[] buffer = new byte[1024];
					int x = 0;

					while ((x = in.read(buffer)) >= 0) {
						fos.write(buffer, 0, x);
						downloaded += x;

						publishProgress(downloaded);

					}

					fos.close();

				} catch (Exception e) {

					return e + "";
				}

				return "complete";

			}

			@Override
			protected void onProgressUpdate(Integer... p) {

				mDownloaded_part7 = p[0];

				long progress1 = p[0];
				long progress = progress1 * 100 / mTotal_part;

				mProgressBar7_int = (int) progress;

				mDownloaded_part7_info_T = "Receiving data...";

				String unit = " Bytes";

				double downloaded = p[0];
				double size = downloaded;

				if (size > 1024) {
					size = downloaded / 1024;
					unit = " KB";
				}
				if (size > 1024) {
					size = downloaded / 1024 / 1024;
					unit = " MB";
				}
				DecimalFormat decimalFormat = new DecimalFormat("#.##");

				mDownloaded_part7_T = decimalFormat.format(size) + unit;
			}

			@Override
			protected void onPostExecute(String result) {

				if (result.equals("complete")) {
					mDownloaded_part7_info_T = "Complete";

					mProgressBar7_int = 100;

					mDownloaded_part7 = file.length();

					isDownloading_part7 = false;

				}

				else {
					mDownloaded_part7_info_T = "Disconnect";

				}

			}

		};

		// TODO PART8
		part8 = new AsyncTask<Integer, Integer, String>() {

			FileOutputStream fos;
			File file;

			@Override
			protected void onCancelled() {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}

				}
				super.cancel(true);
				mDownloaded_part8_info_T = "Disconnect";

			}

			@Override
			protected void onPreExecute() {
				mDownloaded_part8_info_T = "Sending GET...";
			}

			@Override
			protected String doInBackground(Integer... arg) {

				int start = arg[0];
				int downloaded = 0;
				file = new File("/sdcard/madmanager/temp/" + mFilename
						+ ".part8");

				try {

					URL url = new URL(mLink);

					int parts = mParts - 1;
					int size1 = (int) mTotal;
					int size2 = size1 - (mTotal_part * parts);
					if (file.length() == size2) {
						return "complete";
					}

					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();

					if (file.exists()) {
						downloaded = (int) file.length();
						start = (int) (arg[0] + file.length());
					}

					connection.setRequestProperty("Range", "bytes=" + start
							+ "-");

					connection.setDoInput(true);
					connection.setDoOutput(true);

					InputStream in = connection.getInputStream();

					fos = (downloaded == 0) ? new FileOutputStream(file)
							: new FileOutputStream(file, true);

					byte[] buffer = new byte[1024];
					int x = 0;

					while ((x = in.read(buffer)) >= 0) {
						fos.write(buffer, 0, x);
						downloaded += x;

						publishProgress(downloaded);

					}

					fos.close();

				} catch (Exception e) {

					return e + "";
				}

				return "complete";

			}

			@Override
			protected void onProgressUpdate(Integer... p) {

				mDownloaded_part8 = p[0];

				long progress1 = p[0];
				long progress = progress1 * 100 / mTotal_part;

				mProgressBar8_int = (int) progress;

				mDownloaded_part8_info_T = "Receiving data...";

				String unit = " Bytes";

				double downloaded = p[0];
				double size = downloaded;

				if (size > 1024) {
					size = downloaded / 1024;
					unit = " KB";
				}
				if (size > 1024) {
					size = downloaded / 1024 / 1024;
					unit = " MB";
				}
				DecimalFormat decimalFormat = new DecimalFormat("#.##");

				mDownloaded_part8_T = decimalFormat.format(size) + unit;

			}

			@Override
			protected void onPostExecute(String result) {

				if (result.equals("complete")) {
					mDownloaded_part8_info_T = "Complete";

					mProgressBar8_int = 100;

					mDownloaded_part8 = file.length();

					isDownloading_part8 = false;

				}

				else {
					mDownloaded_part8_info_T = "Disconnect";

				}

			}

		};

	}

	public static class join extends AsyncTask<String, Integer, String> {

		File mainfile = new File("/sdcard/madmanager/" + mFilename);

		@Override
		protected void onPreExecute() {

			mStatus_T = "Appending parts...";

			isJoining = true;
		}

		@Override
		protected String doInBackground(String... arg) {

			// TODO remove or change
			if (mainfile.exists()) {
				mainfile.delete();
			}

			try {

				File dir = new File("/sdcard/madmanager/temp/");

				File[] files = dir.listFiles();

				Comparator<? super File> filecomparator = new Comparator<File>() {
					public int compare(File file1, File file2) {
						return String.valueOf(file1.getName()).compareTo(
								file2.getName());
					}
				};

				Arrays.sort(files, filecomparator);

				int written = 0;

				for (File file : files) {

					if (file.getName().contains(mFilename)) {
						FileInputStream fis = new FileInputStream(file);

						FileOutputStream fos = new FileOutputStream(mainfile,
								true);

						byte[] data = new byte[4096];
						int x = 0;

						while ((x = fis.read(data)) >= 0) {
							fos.write(data, 0, x);

							written += x;

							publishProgress(written);
						}
						file.delete();
					}

				}

			} catch (Exception e) {
				return "Error: " + e;

			}

			return "done";

		}

		@Override
		protected void onProgressUpdate(Integer... p) {

			long progress1 = p[0];
			long progress = progress1 * 100 / mTotal;

			DownloadActivity.mProgressBar_Main.setProgress((int) progress);

		}

		@Override
		protected void onPostExecute(String result) {

			isJoining = false;
			isJoined = true;

			if (result.equals("done")) {
				new md5().execute();
			} else if (result.contains("Error")) {
				mStatus_T = "Error";

			}

		}

	}

	public static class md5 extends AsyncTask<String, Integer, String> {

		String md5 = "";

		@Override
		protected String doInBackground(String... arg) {

			File file = new File("/sdcard/madmanager/" + mFilename);

			md5 = MD5Checksum.getMD5Checksum(file.getAbsolutePath());

			return md5;

		}

		@Override
		protected void onPostExecute(String result) {

			if (result.equals(mMD5) || mMD5.equals("")) {
				mStatus_T = "Complete";

			} else {
				mStatus_T = "Error";
			}
		}

	}

	public static boolean isDownloading() {
		if (isDownloading_part1 || isDownloading_part2 || isDownloading_part3
				|| isDownloading_part4 || isDownloading_part5
				|| isDownloading_part6 || isDownloading_part7
				|| isDownloading_part8) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isCompleted_Really() {
		if (isCompleted() && isJoined && mStatus_T.equals("Complete")) {
			return true;
		} else {
			return false;
		}
	}

	public static void cancel() {

		part1.cancel(true);
		part2.cancel(true);
		part3.cancel(true);
		part4.cancel(true);
		part5.cancel(true);
		part6.cancel(true);
		part7.cancel(true);
		part8.cancel(true);

		cancelled = true;

	}

	public static long mDownloaded_long() {
		long l = mDownloaded_part1 + mDownloaded_part2 + mDownloaded_part3
				+ mDownloaded_part4 + mDownloaded_part5 + mDownloaded_part6
				+ mDownloaded_part7 + mDownloaded_part8;

		return l;
	};

	public static boolean isCompleted() {
		if ((mDownloaded_long() == mTotal) && !isDownloading()) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {

		cancel();

		mLink = "";
		mFilename = "";
		mTotal = 1;

		mDownloaded_part1 = 0;
		mDownloaded_part2 = 0;
		mDownloaded_part3 = 0;
		mDownloaded_part4 = 0;
		mDownloaded_part5 = 0;
		mDownloaded_part6 = 0;
		mDownloaded_part7 = 0;
		mDownloaded_part8 = 0;

		isDownloading_part1 = true;
		isDownloading_part2 = true;
		isDownloading_part3 = true;
		isDownloading_part4 = true;
		isDownloading_part5 = true;
		isDownloading_part6 = true;
		isDownloading_part7 = true;
		isDownloading_part8 = true;

		mTotal_part = 1;

		mFilesize_T = "";
		mStatus_T = "";

		mDownloaded_part1_info_T = "";
		mDownloaded_part2_info_T = "";
		mDownloaded_part3_info_T = "";
		mDownloaded_part4_info_T = "";
		mDownloaded_part5_info_T = "";
		mDownloaded_part6_info_T = "";
		mDownloaded_part7_info_T = "";
		mDownloaded_part8_info_T = "";

		mDownloaded_part1_T = "";
		mDownloaded_part2_T = "";
		mDownloaded_part3_T = "";
		mDownloaded_part4_T = "";
		mDownloaded_part5_T = "";
		mDownloaded_part6_T = "";
		mDownloaded_part7_T = "";
		mDownloaded_part8_T = "";

		mProgressBar1_int = 0;
		mProgressBar2_int = 0;
		mProgressBar3_int = 0;
		mProgressBar4_int = 0;
		mProgressBar5_int = 0;
		mProgressBar6_int = 0;
		mProgressBar7_int = 0;
		mProgressBar8_int = 0;

		isJoining = false;
		isJoined = false;
		cancelled = false;
		mRomIcon = null;

		super.onDestroy();
	}

}
