package co.madteam.madmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.madteam.madmanager.utilities.MadUtils;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.analytics.tracking.android.EasyTracker;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class RecoveryList extends SherlockPreferenceActivity {

	public static Context mContext;
	private PreferenceScreen PrefScreen;
	private boolean isDestroyed;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			super.onBackPressed();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setTheme(Theme.getTheme(this));

		requestWindowFeature(Window.FEATURE_PROGRESS);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);

		isDestroyed = false;

		setContentView(R.layout.pref_screen);

		EasyTracker.getInstance().activityStart(this);

		mContext = this;

		String device = Devices.getDeviceName();

		String RecoveryList = "http://www.2-si.net/_roms/?do=json&sa=recovery&device="
				+ device;

		new RecoveryListAsync().execute(RecoveryList);

	}

	public class RecoveryListAsync extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);

		}

		@Override
		protected String doInBackground(String... arg) {
			PrefScreen = getPreferenceManager()
					.createPreferenceScreen(mContext);

			try {
				URL readJSON = new URL(arg[0]);
				URLConnection tc = readJSON.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						tc.getInputStream()));

				String line;

				while ((line = in.readLine()) != null) {

					JSONObject jObjectMain = new JSONObject(line);
					JSONArray jArrayList = jObjectMain.getJSONArray("recovery");

					Preference header = new Preference(mContext);
					header.setSummary(getText(R.string.official_cwm_not_supported));
					header.setEnabled(true);
					header.setLayoutResource(R.layout.pref_warning);
					header.setSelectable(false);
					PrefScreen.addPreference(header);

					for (int j = 0; j < jArrayList.length(); j++) {
						JSONObject jo = (JSONObject) jArrayList.get(j);

						Preference ROMs = new Preference(mContext);
						ROMs.setTitle(jo.getString("name"));
						if (!MadUtils.isEmpty(jo.getString("summary"))) {
							ROMs.setSummary(jo.getString("summary"));
						}
						ROMs.setEnabled(true);
						ROMs.setLayoutResource(R.layout.pref);
						ROMs.setKey("{" + "file" + "=\"" + jo.getString("file")
								+ "\", " + "url" + "=\"" + jo.getString("url")
								+ "\", " + "md5" + "=\"" + jo.getString("md5")
								+ "\", " + "name" + "=\""
								+ jo.getString("name") + "\", " + "summary"
								+ "=\"" + jo.getString("summary") + "\"}"

						);

						PrefScreen.addPreference(ROMs);
					}

					setPreferenceScreen(PrefScreen);

					return null;

				}

			} catch (MalformedURLException e) {
				return getText(R.string.unable_to_connect).toString();
			} catch (IOException e) {
				return getText(R.string.unable_to_connect).toString();
			} catch (JSONException e) {
				return getText(R.string.no_recovery_found).toString();
			}
			return null;

		}

		@Override
		protected void onPostExecute(String result) {

			if (result != null) {
				Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
			}

			setSupportProgressBarIndeterminateVisibility(false);

		}
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			final Preference preference) {

		String file = null;
		String url = null;
		String md5 = null;
		String name = null;
		String summary = null;

		try {
			JSONObject jo = new JSONObject(preference.getKey());
			file = jo.getString("file");
			url = jo.getString("url");
			md5 = jo.getString("md5");
			name = jo.getString("name");
			summary = jo.getString("summary");

		} catch (JSONException e) {
		}

		new DownloadFlashRecovery(file, url, md5, name, summary);
		return false;
	}

	public class DownloadFlashRecovery {

		private String mTitle;
		private String mSummary;
		private String mFilename;
		private String mMD5;
		private String mURL;

		DownloadFlashRecovery(String filename, String url, String md5,
				String title, String summary) {
			mTitle = title;
			mSummary = summary;
			mFilename = filename;
			mMD5 = md5;
			mURL = url;
			DownloadRecovery();
		}

		public void FlashDialog() {

			AlertDialog.Builder flash = new AlertDialog.Builder(mContext);
			flash.setTitle(getText(R.string.download_complete));
			flash.setMessage(getText(R.string.download_recovery_complete));
			flash.setPositiveButton(getText(R.string.flash),
					new OnClickListener() {

						public void onClick(DialogInterface arg0, int arg1) {

							new FlashRecovery().execute(mFilename, mMD5);

						}
					});

			flash.setNegativeButton(getText(R.string.cancel), null);
			flash.show();

		}

		public void ErrorDialog() {

			AlertDialog.Builder error = new AlertDialog.Builder(mContext);
			error.setMessage(getText(R.string.download_recovery_failed));
			error.setNegativeButton(getText(R.string.ok), null);
			error.show();

		}

		public void DownloadDialog() {

			AlertDialog.Builder flash = new AlertDialog.Builder(mContext);
			flash.setTitle(mContext.getText(R.string.download_recovery));
			flash.setMessage(mTitle + "\n" + mSummary + "\n\n"
					+ mContext.getText(R.string.while_dwnling_cwm_msg));
			flash.setPositiveButton(mContext.getText(R.string.dnbtn),
					new OnClickListener() {

						public void onClick(DialogInterface arg0, int arg1) {
							DownloadRecovery();

						}
					});

			flash.setNegativeButton(mContext.getText(R.string.cancel), null);
			flash.show();
		}

		public void DownloadRecovery() {

			new AsyncTask<Void, String, String>() {

				@Override
				protected void onPreExecute() {

					setSupportProgressBarIndeterminateVisibility(true);
					PrefScreen.setEnabled(false);
					Toast.makeText(mContext,
							getText(R.string.starting_download),
							Toast.LENGTH_SHORT).show();
				}

				@Override
				protected void onPostExecute(String result) {
					if (!isDestroyed) {
						PrefScreen.setEnabled(true);
					}
				}

				@Override
				protected String doInBackground(Void... arg) {

					if (!DownloadFlashImage()) {
						publishProgress("3");
						return null;
					}

					Command.checkDir(new File(Environment
							.getExternalStorageDirectory().getPath()
							+ "/madmanager/recovery"));

					File destination = new File(Environment
							.getExternalStorageDirectory().getPath()
							+ "/madmanager/recovery/" + mFilename);

					try {
						URL u = new URL(mURL);

						if (destination.exists()
								&& MD5Checksum.verifyMD5Checksum(
										destination.getAbsolutePath(), mMD5)) {
							publishProgress("1", "100");
							publishProgress("2");
							return null;
						}

						HttpURLConnection c = (HttpURLConnection) u
								.openConnection();
						c.setRequestMethod("GET");

						c.setDoOutput(true);
						c.connect();

						int lenghtOfFile = c.getContentLength();

						FileOutputStream fileOutputStream = new FileOutputStream(
								destination);
						InputStream in = c.getInputStream();

						byte[] buffer = new byte[1024];
						int len1 = 0;
						long total = 0;

						while ((len1 = in.read(buffer)) > 0) {
							total += len1;
							publishProgress("1", ""
									+ (int) ((total * 100) / lenghtOfFile));
							fileOutputStream.write(buffer, 0, len1);
							if (isDestroyed) {
								fileOutputStream.close();
								return null;
							}
						}
						fileOutputStream.close();

					} catch (Exception e) {
						publishProgress("3");
						return null;
					}

					if (destination.exists()
							&& MD5Checksum.verifyMD5Checksum(
									destination.getAbsolutePath(), mMD5)) {
						publishProgress("1", "100");
						publishProgress("2");
						return null;
					} else {
						publishProgress("3");
					}

					return null;
				}

				@Override
				protected void onProgressUpdate(String... progress) {

					if (!isDestroyed) {
						switch (Integer.parseInt(progress[0])) {
						case 1:
							setSupportProgress(Integer.parseInt(progress[1]) * 100);
							break;
						case 2:
							FlashDialog();
							break;
						case 3:
							ErrorDialog();
							setSupportProgressBarIndeterminateVisibility(false);
							break;

						}
					}

				}

			}.execute();
		}

		public boolean DownloadFlashImage() {

			Command.checkDir(new File("/data/data/co.madteam.madmanager/files"));

			String device = null;
			String url = null;
			String md5 = null;

			File destination = new File(
					"/data/data/co.madteam.madmanager/files/"
							+ Devices.getDeviceName());

			try {

				URL readJSON = new URL(
						"http://www.2-si.net/_roms/?do=json&sa=image&device="
								+ Devices.getDeviceName());
				URLConnection tc = readJSON.openConnection();
				BufferedReader JSONin = new BufferedReader(
						new InputStreamReader(tc.getInputStream()));

				String line;

				while ((line = JSONin.readLine()) != null) {

					JSONObject jo = new JSONObject(line);

					device = jo.getString("device");
					url = jo.getString("url");
					md5 = jo.getString("md5");

				}

				if (MadUtils.isEmpty(device) || MadUtils.isEmpty(url)
						|| MadUtils.isEmpty(md5)) {
					return false;
				}

				if (!device.equals(Devices.getDeviceName())) {
					return false;
				}

				URL u = new URL(url);

				if (destination.exists()
						&& MD5Checksum.verifyMD5Checksum(
								destination.getAbsolutePath(), md5)) {

					return true;
				}

				HttpURLConnection c = (HttpURLConnection) u.openConnection();
				c.setRequestMethod("GET");
				c.setDoOutput(true);
				c.connect();

				FileOutputStream fileOutputStream = new FileOutputStream(
						destination);
				InputStream in = c.getInputStream();

				byte[] buffer = new byte[1024];
				int len1 = 0;

				while ((len1 = in.read(buffer)) > 0) {
					fileOutputStream.write(buffer, 0, len1);
					if (isDestroyed) {
						fileOutputStream.close();
						return false;
					}
				}
				fileOutputStream.close();

			} catch (Exception e) {
				return false;
			}

			if (destination.exists()
					&& MD5Checksum.verifyMD5Checksum(
							destination.getAbsolutePath(), md5)) {
				return true;
			}

			return false;

		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isDestroyed = true;
		EasyTracker.getInstance().activityStop(this);
	}

}