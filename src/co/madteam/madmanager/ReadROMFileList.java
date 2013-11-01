package co.madteam.madmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import co.madteam.madmanager.utilities.BlurImage;
import co.madteam.madmanager.utilities.MadUtils;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.analytics.tracking.android.EasyTracker;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ReadROMFileList extends SherlockPreferenceActivity {

	private String mURL;
	private String mIconURL;
	private String mDevName;
	private String mRomName;
	private String mDescription;
	private long mDownloads;
	private String mRateCount;
	private String mRating;
	private String mWebpage;
	private Context mContext;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			NavUtils.navigateUpFromSameTask(this);

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setTheme(Theme.getTheme(this));

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.pref_screen_romfilelist);

		EasyTracker.getInstance().activityStart(this);

		mContext = this;

		Bundle bundle = getIntent().getExtras();

		if (bundle == null) {
			NavUtils.navigateUpFromSameTask(this);
		}

		mURL = bundle.getString("jsonurl");
		mIconURL = bundle.getString("iconurl");
		mDevName = bundle.getString("devname");
		mRomName = bundle.getString("romname");
		mDescription = bundle.getString("description");
		mWebpage = bundle.getString("webpage");
		mDownloads = bundle.getLong("downloads", 0);
		mRateCount = bundle.getString("rateCount");
		mRating = bundle.getString("rating");

		if (mURL == null) {
			NavUtils.navigateUpFromSameTask(this);
		}

		Button try_again = (Button) findViewById(R.id.button);
		try_again.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				loadFileList(mURL);
			}
		});

		loadFileList(mURL);
	}

	private void setError(String error) {
		TextView textView = (TextView) findViewById(R.id.msg);
		textView.setText(error);
	}

	private void displayError(boolean bool) {
		ListView listView = (ListView) findViewById(android.R.id.list);
		listView.setVisibility(bool ? ListView.GONE : ListView.VISIBLE);
		TextView textView = (TextView) findViewById(R.id.msg);
		textView.setVisibility(bool ? TextView.VISIBLE : TextView.GONE);
		Button button = (Button) findViewById(R.id.button);
		button.setVisibility(bool ? Button.VISIBLE : Button.GONE);
	}

	private void loadFileList(String url) {

		displayError(false);

		new AsyncTask<String, String, String>() {

			@Override
			protected void onPreExecute() {
				setSupportProgressBarIndeterminateVisibility(true);
			}

			@Override
			protected String doInBackground(String... arg) {

				if (!Command.isNetworkAvailable(mContext)) {
					return getText(R.string.no_connection).toString();
				}

				PreferenceScreen PrefScreen = getPreferenceManager()
						.createPreferenceScreen(mContext);

				try {
					
					String latestVersion = null;

					try {
						String versionUrl = "http://www.2-si.net/_roms/?do=json&sa=verchk&rom=";
						URL versionJSON = new URL(versionUrl + mURL);
						URLConnection versionUC = versionJSON.openConnection();

						BufferedReader versionIN = new BufferedReader(
								new InputStreamReader(
										versionUC.getInputStream()));
						String versionLine;

						while ((versionLine = versionIN.readLine()) != null) {

							JSONObject joMain = new JSONObject(versionLine);
							JSONObject jo = joMain.getJSONObject("rom");
							latestVersion = jo.getString("name");

						}
					} catch (Exception e) {

					}

					URL readJSON = new URL(
							"http://www.2-si.net/_roms/?do=json&sa=rom&rom="
									+ mURL);
					URLConnection tc = readJSON.openConnection();
					BufferedReader in = new BufferedReader(
							new InputStreamReader(tc.getInputStream()));

					String line;

					while ((line = in.readLine()) != null) {

						JSONArray jArrayMain = new JSONArray(line);

						Bitmap icon = null;

						try {
							BitmapFactory.Options bmOptions = new BitmapFactory.Options();
							bmOptions.inSampleSize = 1;

							URL url = new URL(mIconURL);
							URLConnection conn = url.openConnection();

							HttpURLConnection httpConn = (HttpURLConnection) conn;
							httpConn.setRequestMethod("GET");
							httpConn.connect();

							InputStream inputStream = httpConn.getInputStream();

							icon = BitmapFactory.decodeStream(inputStream,
									null, bmOptions);
							inputStream.close();

						} catch (IOException e) {
						}

						PrefROMDescription Description = new PrefROMDescription(
								mContext);
						Description.setTitle(mRomName);
						Description.setSummary(mDescription);
						Description.setDevName(mDevName);
						Description.setBackgroundIcon(new BlurImage()
								.getBlurBitmap(icon, 2));
						Description.setRomIcon(new BlurImage()
								.createResizedBitmap(getResources(), icon, 70,
										70, true));
						Description.setEnabled(true);
						Description.setRateCount(mRateCount);
						Description.setDownloads(String.valueOf(mDownloads));
						Description.setLastestBuild(latestVersion);
						Description.setSelectable(false);
						Description.setRating(mRating);
						PrefScreen.addPreference(Description);

						for (int i = 0; i < jArrayMain.length(); i++) {
							JSONObject jObjectMain = (JSONObject) jArrayMain
									.get(i);
							JSONArray jArrayList = jObjectMain
									.getJSONArray("list");

							PreferenceCategory PrefCat = new PreferenceCategory(
									mContext);
							if (!MadUtils.isEmpty(jObjectMain
									.getString("category"))) {
								PrefCat.setTitle(jObjectMain.getString(
										"category").toUpperCase(Locale.US));
								PrefScreen.addPreference(PrefCat);
							}

							Comparator<? super JSONObject> comparator = new Comparator<JSONObject>() {
								public int compare(JSONObject object1,
										JSONObject object2) {
									try {
										return Integer
												.valueOf(
														object1.getString("id"))
												.compareTo(
														Integer.valueOf(object2
																.getString("id")));
									} catch (JSONException e) {
									} catch (NumberFormatException e) {
									}
									return 0;
								}
							};

							ArrayList<JSONObject> arrayList = new ArrayList<JSONObject>();
							for (int j = 0; j < jArrayList.length(); j++) {
								arrayList.add(jArrayList.getJSONObject(j));
							}

							JSONObject[] jArrayListSorted = new JSONObject[arrayList
									.size()];
							arrayList.toArray(jArrayListSorted);
							Arrays.sort(jArrayListSorted, comparator);

							for (int k = 0; k < jArrayListSorted.length; k++) {
								JSONObject jo = jArrayListSorted[k];

								if (!MadUtils.isEmpty(jo.getString("name"))) {

									Preference ROMs = new Preference(mContext);

									ROMs.setTitle(jo.getString("name"));
									ROMs.setSummary(jo.getString("summary"));
									ROMs.setEnabled(true);
									ROMs.setLayoutResource(R.layout.pref);
									ROMs.setKey(jo.toString());
									PrefCat.addPreference(ROMs);
								}
							}

							/*
							 * 
							 * for (int j = 0; j < jArrayList.length(); j++) {
							 * JSONObject jo = (JSONObject) jArrayList.get(j);
							 * 
							 * if (!MadUtils.isEmpty(jo.getString("name"))) {
							 * 
							 * Preference ROMs = new Preference(mContext);
							 * 
							 * ROMs.setTitle(jo.getString("name"));
							 * ROMs.setSummary(jo.getString("summary"));
							 * ROMs.setEnabled(true);
							 * ROMs.setLayoutResource(R.layout.pref);
							 * ROMs.setKey("{" + "romname" + "=\"" +
							 * jo.getString("name") + "\", " + "filename" +
							 * "=\"" + jo.getString("filename") + "\", " + "url"
							 * + "=\"" + jo.getString("url") + "\", " +
							 * "isGooIM" + "=\"" + jo.getString("isGooIM") +
							 * "\", " + "id" + "=\"" + jo.getString("id") +
							 * "\", " + "dev" + "=\"" + jo.getString("dev") +
							 * "\", " + "icon" + "=\"" + jo.getString("icon") +
							 * "\", " + "md5" + "=\"" + jo.getString("md5") +
							 * "\", " + "rateCount" + "=\"" +
							 * jo.getString("rateCount") + "\", " + "rating" +
							 * "=\"" + jo.getString("rating") + "\", " +
							 * "downloads" + "=\"" + jo.getString("downloads") +
							 * "\", " + "summary" + "=\"" +
							 * jo.getString("summary") + "\"}");
							 * PrefCat.addPreference(ROMs); } }
							 */
						}
					}
				} catch (IOException e) {
					return getText(R.string.network_error).toString();
				} catch (JSONException e) {
					return getText(R.string.no_releases_found).toString();
				}

				setPreferenceScreen(PrefScreen);

				return null;
			}

			@Override
			protected void onPostExecute(String result) {

				if (result != null) {
					setError(result);
					displayError(true);
				}
				setSupportProgressBarIndeterminateVisibility(false);

			}

		}.execute();

	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			final Preference preference) {

		String romname = null;
		String filename = null;
		String url = null;
		String isGooIM = null;
		String id = null;
		String dev = null;
		String icon = null;
		String md5 = null;
		String rateCount = null;
		String rating = null;
		String downloads = null;
		String summary = null;

		try {
			String key = preference.getKey();
			if (key == null) {
				return false;
			}
			JSONObject jo = new JSONObject(key);
			romname = jo.getString("name");
			filename = jo.getString("filename");
			url = jo.getString("url");
			isGooIM = jo.getString("isGooIM");
			id = jo.getString("id");
			dev = jo.getString("dev");
			icon = jo.getString("icon");
			md5 = jo.getString("md5");
			rateCount = jo.getString("rateCount");
			rating = jo.getString("rating");
			downloads = jo.getString("downloads");
			summary = jo.getString("summary");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		Bundle extras = new Bundle();

		extras.putString("romname", romname);
		extras.putString("filename", filename);
		extras.putString("url", url);
		extras.putString("isGooIM", isGooIM);
		extras.putString("id", id);
		extras.putString("dev", dev);
		extras.putString("icon", icon);
		extras.putString("md5", md5);
		extras.putString("rateCount", rateCount);
		extras.putString("rating", rating);
		extras.putString("downloads", downloads);
		extras.putString("summary", summary);
		extras.putString("description", mDescription);
		extras.putString("romlisturl", mURL);
		extras.putString("romlistromname", mRomName);
		extras.putString("romlistrateCount", mRateCount);
		extras.putString("romlistrating", mRating);
		extras.putLong("romlistdownloads", mDownloads);
		extras.putString("webpage", mWebpage);

		Intent newDownload = new Intent(this, NewDownload.class)
				.putExtras(extras);
		startActivity(newDownload);

		return false;
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

}