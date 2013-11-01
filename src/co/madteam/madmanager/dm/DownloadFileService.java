package co.madteam.madmanager.dm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import co.madteam.madmanager.MD5Checksum;
import co.madteam.madmanager.NewDownload;
import co.madteam.madmanager.R;
import co.madteam.madmanager.utilities.BlurImage;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class DownloadFileService extends Service {

	private static ArrayList<Bundle> mArrayList = new ArrayList<Bundle>();
	private static AsyncTask<Integer, Long, String> mAsyncTask;
	private static Handler mDownloadHandler;
	private static int mNoOfDownloads;
	private static int mRomID = -1;
	private static boolean isRunning;
	
	private int mProgress;
	private int mCurrentDownload;
	private int mID = 555;
	private int mNoOfConnections;
	private int mPartCompleted;
	private int mPartCancelled;
	private long mPartLength = 1;
	private long mDownloadedLast;
	private long mDownloaded;
	private long mFileLength = 1;

	private boolean noLongerUpdateNoti;
	private boolean isFirstTimeNoti;
	private boolean isCancelledByError;
	private boolean useSystemNotification;

	private ArrayList<DownloadPart> mThreadList = new ArrayList<DownloadPart>();
	private Context mContext;
	private File mFile;
	private Bitmap mRomIcon;
	private NotificationManager mNotificationManager;
	private Handler mUpdateNotiHandler;
	private PendingIntent mPendingIntentDownloads;
	private PendingIntent mPendingIntentDownloadScreen;

	private String URL;
	private String FILENAME;
	private String MD5;
	private String DOWNLOAD_DIRECTORY;
	private String TEMP_DIRECTORY;
	private String ERROR;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public static void addDownload(Context context, Bundle bundle) {

		mArrayList.add(bundle);
		mNoOfDownloads += 1;

		if (!isRunning) {
			context.startService(new Intent(context, DownloadFileService.class));

			Toast.makeText(context,
					context.getText(R.string.download_will_start_shorly),
					Toast.LENGTH_LONG).show();

		} else {
			Toast.makeText(context, context.getText(R.string.download_queued),
					Toast.LENGTH_LONG).show();
		}

	}

	private Runnable updateNotification = new Runnable() {
		public void run() {

			mProgress = (int) (mDownloaded * 100 / mFileLength);

			if (mProgress <= 100 && !noLongerUpdateNoti && isRunning) {

				String space = "   ";
				String DOWNLOADS = noOfDownloads();

				if (DOWNLOADS == null) {
					DOWNLOADS = "";
				}
				long speed1 = (long) (mDownloaded - mDownloadedLast);
				long speed2 = speed1 / 1024;
				long speed = speed2 / 2;
				String unit = "KB/s";

				String SPEED = speed + unit;
				String PROGRESS = mProgress + "%";

				if (isFirstTimeNoti) {
					SPEED = "0KB/s";
					isFirstTimeNoti = false;
				}

				String info = DOWNLOADS + space + SPEED + space + PROGRESS;
				String defaultNotiInfo = (mDownloaded / 1024) + "KB" + "/"
						+ (mFileLength / 1024) + "KB" + " (" + PROGRESS + ", "
						+ SPEED + ", " + DOWNLOADS + ")";

				NotificationBuilder nb = new NotificationBuilder(mContext)
						.setOngoing(true)
						.setContentIntent(mPendingIntentDownloadScreen)
						.setContentTitle(FILENAME)
						.setSmallIcon(R.drawable.ic_noti_download2)
						.setNotiIcon(R.drawable.ic_noti_download)
						.setLargeIcon(mRomIcon)
						.setContentText(getText(R.string.downloading))
						.setContentInfo(info)
						.setWhen(System.currentTimeMillis()).setShowWhen(false)
						.setProgress(100, mProgress, false)
						.setLatestEventTitle(FILENAME)
						.setLatestEventText(defaultNotiInfo)
						.setUseDefaultNotification(useSystemNotification);

				int nth = (mNoOfDownloads + 1) - mCurrentDownload;
				if (nth != 1) {
					nb.setNumber(nth);
				}

				mNotificationManager.notify(mID, nb.build());

				mDownloadedLast = mDownloaded;

			}

			mUpdateNotiHandler.postDelayed(this, 1950);
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();

		mContext = this;

		isRunning = true;

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		DOWNLOAD_DIRECTORY = DownloadManager.downloadDirectory(this);

		TEMP_DIRECTORY = DOWNLOAD_DIRECTORY + "/temp";

		File directory = new File(DOWNLOAD_DIRECTORY);
		if (!directory.exists()) {
			directory.mkdirs();
		}

		File temp = new File(TEMP_DIRECTORY);
		if (!temp.exists()) {
			temp.mkdirs();
		}

		boolean isFastMode = sharedPreferences.getBoolean("fast_download_mode",
				false);

		useSystemNotification = sharedPreferences.getBoolean(
				"use_default_noti", false);

		mNoOfConnections = sharedPreferences.getInt("no_of_connections", 8);

		if (!isFastMode) {
			mNoOfConnections = 1;
		}

		mNotificationManager = (NotificationManager) mContext
				.getSystemService(NOTIFICATION_SERVICE);

		Intent intent = new Intent(mContext, DownloadManager.class);

		mPendingIntentDownloads = PendingIntent.getActivity(mContext, 0,
				intent, 0);

		mUpdateNotiHandler = new Handler();
		mUpdateNotiHandler.post(updateNotification);

		mDownloadHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int code = msg.what;
				switch (code) {
				case 0:
					mPartCompleted += 1;
					if (mPartCompleted == mNoOfConnections) {
						if (mNoOfConnections == 1) {
							downloadComplete("rename");
						} else {
							downloadComplete("join");
						}
					}
					break;
				case 1:
					if (isRunning) {
						isCancelledByError = true;
						downloadComplete("error");
						this.sendEmptyMessage(3);
					}
					break;
				case 2:
					mPartCancelled += 1;

					if (isRunning && !isCancelledByError
							&& (mPartCancelled == mNoOfConnections)) {
						downloadComplete("cancelled");
					}
					break;
				case 3:
					if (!mThreadList.isEmpty()) {
						for (int i = 0; i < mThreadList.size(); i++) {
							mThreadList.get(i).cancelDownload();
						}
					}
					break;
				}

			}

		};

		doNext();
	}

	private String noOfDownloads() {
		String downloads = mCurrentDownload + " of " + mNoOfDownloads;
		if (mCurrentDownload > mNoOfDownloads
				|| (mCurrentDownload == 0 && mNoOfDownloads == 0)) {
			downloads = null;
		}
		return downloads;
	}

	private void doNext() {
		if (mArrayList.isEmpty()) {
			stopSelf();
		} else {
			startDownload(mArrayList.get(0));
		}
	}

	private void downloadComplete(String result) {

		noLongerUpdateNoti = true;

		stopForeground(true);

		if (result.equals("complete")) {

			new checkMD5().execute();

		}

		else if (result.equals("join")) {

			new joinParts().execute();

		}

		else if (result.equals("rename")) {

			new renamePart().execute();

		}

		else if (result.equals("cancelled")) {

			NotificationBuilder nb = new NotificationBuilder(mContext)
					.setAutoCancel(true)
					.setOngoing(false)
					.setContentIntent(mPendingIntentDownloads)
					.setContentTitle(getText(R.string.download_interrupted))
					.setSmallIcon(R.drawable.ic_noti_download2)
					.setNotiIcon(R.drawable.ic_noti_download)
					.setLargeIcon(mRomIcon)
					.setContentText(FILENAME)
					.setWhen(System.currentTimeMillis())
					.setLatestEventTitle(getText(R.string.download_interrupted))
					.setLatestEventText(FILENAME)
					.setUseDefaultNotification(useSystemNotification);

			mNotificationManager.notify(mRomID, nb.build());

			doNext();

		}

		else if (result.equals("error")) {

			NotificationBuilder nb = new NotificationBuilder(mContext)
					.setAutoCancel(true).setOngoing(false)
					.setContentIntent(mPendingIntentDownloads)
					.setContentTitle(getText(R.string.download_error))
					.setSmallIcon(R.drawable.ic_noti_download2)
					.setNotiIcon(R.drawable.ic_noti_download)
					.setLargeIcon(mRomIcon).setContentText(FILENAME)
					.setWhen(System.currentTimeMillis()).setSubText(ERROR)
					.setLatestEventTitle(getText(R.string.download_error))
					.setLatestEventText(FILENAME)
					.setUseDefaultNotification(useSystemNotification);

			mNotificationManager.notify(mRomID, nb.build());

			doNext();

		}

	}

	private void startDownload(final Bundle bundle) {

		mAsyncTask = new AsyncTask<Integer, Long, String>() {

			@Override
			protected void onPreExecute() {

				Bundle extras = bundle.getBundle("extras");

				Intent intent = new Intent(mContext, DownloadManager.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				if (extras != null) {
					intent = new Intent(mContext, NewDownload.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtras(extras);
				}

				mPendingIntentDownloadScreen = PendingIntent.getActivity(
						mContext, mID, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);

				mFileLength = 1;
				mProgress = 0;
				mDownloaded = 0;
				mDownloadedLast = 0;

				noLongerUpdateNoti = true;
				isFirstTimeNoti = true;
				isCancelledByError = false;

				mCurrentDownload += 1;
				mPartCompleted = 0;
				mPartCancelled = 0;

				FILENAME = bundle.getString("filename");
				if (FILENAME == null) {
					FILENAME = "unknown";
				}
				MD5 = bundle.getString("md5");
				URL = bundle.getString("url");
				mRomID = bundle.getInt("romid");
				Bitmap icon = bundle.getParcelable("icon");
				if (icon != null) {
					mRomIcon = new BlurImage().createResizedBitmap(getResources(), icon, 100, 100, true);
				}
				ERROR = null;
				
				mArrayList.remove(bundle);
				mThreadList.clear();

				mFile = new File(DOWNLOAD_DIRECTORY + "/" + FILENAME);

				NotificationBuilder nb = new NotificationBuilder(mContext)
						.setOngoing(true)
						.setContentIntent(mPendingIntentDownloadScreen)
						.setContentTitle(FILENAME)
						.setSmallIcon(R.drawable.ic_noti_download2)
						.setNotiIcon(R.drawable.ic_noti_download)
						.setLargeIcon(mRomIcon)
						.setContentText(getText(R.string.connecting))
						.setContentInfo(noOfDownloads())
						.setWhen(System.currentTimeMillis()).setShowWhen(false)
						.setProgress(100, mProgress, true)
						.setLatestEventTitle(FILENAME)
						.setLatestEventText(getText(R.string.connecting))
						.setUseDefaultNotification(useSystemNotification);

				int nth = (mNoOfDownloads + 1) - mCurrentDownload;
				if (nth != 1) {
					nb.setNumber(nth);
				}
				Notification notification = nb.build();

				mNotificationManager.notify(mID, notification);

				startForeground(mID, notification);

			}

			@Override
			protected String doInBackground(Integer... arg) {

				try {

					URL url = new URL(URL);

					mFileLength = url.openConnection().getContentLength();

					if (mFileLength == -1) {
						return "error";
					}

					if (mFile.exists() && mFile.length() == mFileLength) {
						return "complete";
					}
					mPartLength = mFileLength / mNoOfConnections;

				} catch (IOException e) {
					ERROR = e.getMessage();
					return "error";
				}

				if (mNoOfConnections == 1) {

					DownloadPart thread = new DownloadPart(1, 0, 0, true);
					thread.start();
					mThreadList.add(thread);

				} else {
					for (int i = 1; i <= mNoOfConnections; i++) {

						int start = (int) (mPartLength * (i - 1));
						int end = (int) (mPartLength * i) - 1;

						if (i == mNoOfConnections) {
							DownloadPart thread = new DownloadPart(i, start,
									end, true);
							thread.start();
							mThreadList.add(thread);
						} else {
							DownloadPart thread = new DownloadPart(i, start,
									end, false);
							thread.start();
							mThreadList.add(thread);
						}
					}
				}

				return "inprogress";

			}

			@Override
			protected void onCancelled() {
				downloadComplete("cancelled");
				super.cancel(true);
			}

			@Override
			protected void onPostExecute(String result) {

				if (!result.equals("inprogress")) {
					downloadComplete(result);
				}

			}

		}.execute(mID);

	}

	private class DownloadPart extends Thread {

		int pID;
		long pDownloaded;
		long pStart;
		long pEnd;
		long pPartLength;
		long pRemainingFileLengthCalculated;
		long pRemainingFileLength;
		boolean isCancelled;
		boolean isLastPart;
		FileOutputStream fileOutputStream = null;

		DownloadPart(int id, long start, long end, boolean islastpart) {
			pID = id;
			pStart = start;
			pEnd = end;
			isLastPart = islastpart;
			isCancelled = false;
		}

		public void cancelDownload() {
			isCancelled = true;
			mDownloadHandler.sendEmptyMessage(2);

		}

		@Override
		public void run() {

			try {

				URL url = new URL(URL);
				File file = null;
				if (mNoOfConnections == 1) {
					file = new File(TEMP_DIRECTORY + "/" + FILENAME + ".tmp");
				} else {
					file = new File(TEMP_DIRECTORY + "/" + FILENAME + ".part"
							+ pID);
				}
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();

				if (isLastPart) {
					pPartLength = mFileLength
							- (mPartLength * (mNoOfConnections - 1));
				} else {
					pPartLength = mPartLength;
				}

				if (file.exists()) {
					pDownloaded = file.length();
					if (pDownloaded == pPartLength) {
						mDownloaded += pDownloaded;
						mDownloadHandler.sendEmptyMessage(0);
						return;

					} else if (pDownloaded > pPartLength) {
						pDownloaded = 0;
					} else {

						pRemainingFileLengthCalculated = pPartLength
								- pDownloaded;
						mDownloaded += pDownloaded;
					}
				}

				if (isLastPart) {

					connection.setRequestProperty("Range", "bytes="
							+ (pStart + pDownloaded) + "-");
				} else {

					connection.setRequestProperty("Range", "bytes="
							+ (pStart + pDownloaded) + "-" + pEnd);
				}

				connection.setDoInput(true);
				connection.setDoOutput(true);

				pRemainingFileLength = connection.getContentLength();
				if (pRemainingFileLength == -1) {

					mDownloadHandler.sendEmptyMessage(1);
					return;
				} else if (pRemainingFileLength != pRemainingFileLengthCalculated
						&& pDownloaded != 0) {

					mDownloaded -= pDownloaded;
					pDownloaded = 0;

					connection = (HttpURLConnection) url.openConnection();

					if (isLastPart) {
						connection.setRequestProperty("Range", "bytes="
								+ pStart + "-");
					} else {
						connection.setRequestProperty("Range", "bytes="
								+ pStart + "-" + pEnd);
					}
					connection.setDoInput(true);
					connection.setDoOutput(true);
				}

				long length = connection.getContentLength();
				if (length == -1) {
					mDownloadHandler.sendEmptyMessage(1);
				}
				if (mNoOfConnections != 1 && length == mFileLength) {
					mDownloadHandler.sendEmptyMessage(1);
				}

				InputStream in = connection.getInputStream();

				fileOutputStream = (pDownloaded == 0) ? new FileOutputStream(
						file) : new FileOutputStream(file, true);

				byte[] buffer = new byte[1024];
				int x = 0;
				while ((x = in.read(buffer)) >= 0) {
					fileOutputStream.write(buffer, 0, x);
					mDownloaded += x;
					pDownloaded += x;
					noLongerUpdateNoti = false;

					if (isCancelled || !isRunning || pDownloaded > pPartLength) {
						break;
					}
				}

				if (file.length() == pPartLength) {
					if (mNoOfConnections == 1) {
						downloadComplete("rename");
					} else {
						mDownloadHandler.sendEmptyMessage(0);
					}
				}
				return;

			} catch (IOException e) {

				Log.e("MAD", e + " Thread " + pID);

				ERROR = e.getMessage();
				mDownloadHandler.sendEmptyMessage(1);

			} finally {
				try {
					if (fileOutputStream != null) {
						fileOutputStream.close();
					}
				} catch (IOException e) {
				}
			}
		}
	}

	private class joinParts extends AsyncTask<Void, String, String> {

		@Override
		protected void onPreExecute() {

			NotificationBuilder nb = new NotificationBuilder(mContext)
					.setOngoing(false)
					.setContentIntent(mPendingIntentDownloadScreen)
					.setContentTitle(FILENAME)
					.setSmallIcon(R.drawable.ic_noti_download2)
					.setNotiIcon(R.drawable.ic_noti_download)
					.setLargeIcon(mRomIcon)
					.setContentText(getText(R.string.download_appending))
					.setWhen(System.currentTimeMillis()).setShowWhen(false)
					.setProgress(100, 0, true).setLatestEventTitle(FILENAME)
					.setLatestEventText(getText(R.string.download_appending))
					.setUseDefaultNotification(useSystemNotification);

			mNotificationManager.notify(mRomID, nb.build());

		}

		@Override
		protected String doInBackground(Void... arg) {

			if (mFile.exists()) {
				mFile.delete();
			}

			try {

				for (int i = 1; i <= mNoOfConnections; i++) {

					File file = new File(TEMP_DIRECTORY + "/" + FILENAME
							+ ".part" + i);

					if (file.getName().contains(FILENAME)) {
						FileInputStream fileInputStream = new FileInputStream(
								file);

						FileOutputStream fileOutputStream = new FileOutputStream(
								mFile, true);

						byte[] data = new byte[4096];
						int x = 0;

						while ((x = fileInputStream.read(data)) >= 0) {
							fileOutputStream.write(data, 0, x);

						}
						file.delete();
					}

				}
				return "complete";

			} catch (Exception e) {
				ERROR = e.getMessage();
				return "error";
			}

		}

		@Override
		protected void onPostExecute(String result) {
			downloadComplete(result);
		}
	}

	private class renamePart extends AsyncTask<Void, String, String> {

		@Override
		protected String doInBackground(Void... arg) {

			if (mFile.exists()) {
				mFile.delete();
			}

			File file = new File(TEMP_DIRECTORY + "/" + FILENAME + ".tmp");
			file.renameTo(mFile);

			return "complete";

		}

		@Override
		protected void onPostExecute(String result) {
			downloadComplete(result);
		}
	}

	private class checkMD5 extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {

			NotificationBuilder nb = new NotificationBuilder(mContext)
					.setOngoing(false)
					.setContentIntent(mPendingIntentDownloadScreen)
					.setContentTitle(FILENAME)
					.setSmallIcon(R.drawable.ic_noti_download2)
					.setNotiIcon(R.drawable.ic_noti_download)
					.setLargeIcon(mRomIcon)
					.setContentText(getText(R.string.verifying_md5))
					.setWhen(System.currentTimeMillis()).setShowWhen(false)
					.setProgress(100, 0, true).setLatestEventTitle(FILENAME)
					.setLatestEventText(getText(R.string.verifying_md5))
					.setUseDefaultNotification(useSystemNotification);

			mNotificationManager.notify(mRomID, nb.build());

		}

		@Override
		protected String doInBackground(Void... arg) {

			return MD5Checksum.getMD5Checksum(mFile.getPath());

		}

		@Override
		protected void onPostExecute(String result) {

			mNotificationManager.cancel(mRomID);

			if (result.equalsIgnoreCase(MD5) || MD5 == null) {

				NotificationBuilder nb = new NotificationBuilder(mContext)
						.setAutoCancel(true)
						.setOngoing(false)
						.setContentIntent(mPendingIntentDownloads)
						.setContentTitle(getText(R.string.download_complete))
						.setSmallIcon(R.drawable.ic_noti_download2)
						.setNotiIcon(R.drawable.ic_noti_download)
						.setLargeIcon(mRomIcon)
						.setContentText(FILENAME)
						.setWhen(System.currentTimeMillis())
						.setLatestEventTitle(
								getText(R.string.download_complete))
						.setLatestEventText(FILENAME)
						.setUseDefaultNotification(useSystemNotification);

				mNotificationManager.notify(mRomID, nb.build());

			} else {

				NotificationBuilder nb = new NotificationBuilder(mContext)
						.setAutoCancel(true).setOngoing(false)
						.setContentIntent(mPendingIntentDownloads)
						.setContentTitle(getText(R.string.md5_mismatch))
						.setSmallIcon(R.drawable.ic_noti_download2)
						.setNotiIcon(R.drawable.ic_noti_download)
						.setLargeIcon(mRomIcon).setContentText(FILENAME)
						.setWhen(System.currentTimeMillis())
						.setLatestEventTitle(getText(R.string.md5_mismatch))
						.setLatestEventText(FILENAME)
						.setUseDefaultNotification(useSystemNotification);

				mNotificationManager.notify(mRomID, nb.build());

			}
			doNext();

		}
	}

	public static int getRomID() {
		if (!isRunning) {
			return -1;
		}
		return mRomID;
	}

	public static boolean isRunning() {
		return isRunning;
	}

	public static boolean cancel(int id) {
		if (id == mRomID) {
			if (mAsyncTask != null) {
				mAsyncTask.cancel(true);
			}
			mDownloadHandler.sendEmptyMessage(3);
			return true;
		} else {
			return false;
		}
	}

	public static void cancelAll() {
		if (mAsyncTask != null) {
			mAsyncTask.cancel(true);
		}
		mDownloadHandler.sendEmptyMessage(3);
	}

	@Override
	public void onDestroy() {

		isRunning = false;

		mNoOfDownloads = 0;
		mCurrentDownload = 0;

		mUpdateNotiHandler.removeCallbacks(updateNotification);

		mDownloadHandler.removeCallbacksAndMessages(null);

		mNotificationManager.cancel(mID);

		mArrayList.clear();
		mThreadList.clear();

		super.onDestroy();
	}

}