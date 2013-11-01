package co.madteam.madmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONException;
import org.json.JSONObject;
import co.madteam.madmanager.dm.NotificationBuilder;
import co.madteam.madmanager.utilities.BlurImage;
import co.madteam.madmanager.utilities.MadUtils;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class CheckUpdateReceiver extends BroadcastReceiver {

	String mRomName;
	String mRomVersion;

	@Override
	public void onReceive(final Context context, Intent intent) {

		mRomName = Command.b("getprop ro.mad.rom");

		mRomVersion = Command.b("getprop ro.mad.version");

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {

				new Thread() {

					@Override
					public void run() {

						if (!MadUtils.isEmpty(mRomName) && !MadUtils.isEmpty(mRomVersion)) {

							int no_of_try = 0;

							while (!Command.isNetworkAvailable(context)) {
								try {
									no_of_try += 1;
									if (no_of_try > 3) {
										return;
									}
									Thread.sleep(600000);
								} catch (InterruptedException e) {
								}
							}

							try {
								String versionUrl = "http://www.2-si.net/_roms/?do=json&sa=verchk&rom=";
								URL readJSON = new URL(versionUrl + mRomName);
								URLConnection tc = readJSON.openConnection();

								BufferedReader in = new BufferedReader(
										new InputStreamReader(
												tc.getInputStream()));
								String line = in.readLine();

								if (line == null) {
									return;
								}

								JSONObject joMain = new JSONObject(line);
								JSONObject jo = joMain.getJSONObject("version");

								String currentRom = mRomVersion
										.replace(" ", "");
								String LatestRom = jo.getString("ver").replace(
										" ", "");

								double current_rom = 0;
								double latest_rom = 0;

								try {
									current_rom = Double
											.parseDouble(currentRom);
									latest_rom = Double.parseDouble(LatestRom);
								} catch (NumberFormatException e) {
								}

								if (latest_rom > current_rom) {

									JSONObject jo2 = joMain
											.getJSONObject("rom");

									String name = jo2.getString("name");
									String filename = jo2.getString("filename");
									String url = jo2.getString("url");
									String isGooIM = jo2.getString("isGooIM");
									String id = jo2.getString("id");
									String dev = jo2.getString("dev");
									String icon = jo2.getString("icon");
									String md5 = jo2.getString("md5");
									String summary = jo2.getString("summary");
									String rateCount = jo2.getString("rateCount");
									String rating = jo2.getString("rating");
									String downloads = jo2.getString("downloads");

									Bundle extras = new Bundle();

									extras.putString("romname", name);
									extras.putString("filename", filename);
									extras.putString("url", url);
									extras.putString("isGooIM", isGooIM);
									extras.putString("id", id);
									extras.putString("dev", dev);
									extras.putString("icon", icon);
									extras.putString("md5", md5);
									extras.putString("summary", summary);
									extras.putString("rateCount", rateCount);
									extras.putString("rating", rating);
									extras.putString("downloads", downloads);

									Bitmap largeIcon = null;

									boolean useDefaultNotification = PreferenceManager
											.getDefaultSharedPreferences(
													context).getBoolean(
													"use_default_noti", false);

									if (!useDefaultNotification) {
										try {
											BitmapFactory.Options bmOptions = new BitmapFactory.Options();
											bmOptions.inSampleSize = 1;

											URLConnection conn = new URL(icon)
													.openConnection();

											HttpURLConnection httpConn = (HttpURLConnection) conn;
											httpConn.setRequestMethod("GET");
											httpConn.connect();

											InputStream inputStream = httpConn
													.getInputStream();

											largeIcon = BitmapFactory
													.decodeStream(inputStream,
															null, bmOptions);
											inputStream.close();

										} catch (IOException e1) {
										}
									}

									Bitmap largeIconScaled = null;
									if (largeIcon != null) {

										largeIconScaled = new BlurImage()
												.createResizedBitmap(
														context.getResources(),
														largeIcon, 100, 100,
														true);

									}

									Intent newDownload = new Intent(context,
											NewDownload.class)
											.putExtras(extras);

									NotificationManager notificationManager = (NotificationManager) context
											.getSystemService("notification");

									PendingIntent pendingIntent = PendingIntent
											.getActivity(
													context,
													0,
													newDownload,
													PendingIntent.FLAG_UPDATE_CURRENT);

									NotificationBuilder nb = new NotificationBuilder(
											context)
											.setAutoCancel(true)
											.setOngoing(false)
											.setContentIntent(pendingIntent)
											.setContentTitle(name)
											.setSmallIcon(
													R.drawable.ic_noti_update)
											.setNotiIcon(
													R.drawable.ic_noti_update)
											.setLargeIcon(largeIconScaled)
											.setContentText(
													context.getText(R.string.update_avail_des))
											.setWhen(System.currentTimeMillis())
											.setLatestEventTitle(name)
											.setLatestEventText(
													context.getText(R.string.update_avail_des))
											.setUseDefaultNotification(
													useDefaultNotification)
											.setTicker(
													context.getText(R.string.update_avail));

									notificationManager.notify(1, nb.build());

								}
							} catch (JSONException e) {
								return;
							} catch (NumberFormatException e) {
								return;
							} catch (IOException e) {
								return;
							}
						}
						return;

					}

				}.run();

				return null;
			}

		}.execute();

	}
}