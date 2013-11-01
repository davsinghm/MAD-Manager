package co.madteam.madmanager;

import java.io.BufferedReader;
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
import co.madteam.madmanager.dm.Download;
import co.madteam.madmanager.dm.DownloadFileService;
import co.madteam.madmanager.utilities.BlurImage;
import co.madteam.madmanager.utilities.MadUtils;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.google.analytics.tracking.android.EasyTracker;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NewDownload extends SherlockPreferenceActivity {

	private static Context mContext;
	private boolean isNonDirectLink;
	private boolean isDestroyed;
	private boolean hasMD5;
	private int mID;
	private String mRating;
	private String mURL;
	private String mName;
	private String mFilename;
	private String mIconURL;
	private String mDev;
	private String mMD5;
	private String mSummary;
	private String mDownloads;
	private String mRateCount;
	private String mWebpage;
	private String mROMListDescription;
	private String mROMListURL;
	private String mROMListRomName;
	private ImageView mIcon;
	private Bitmap mRomIcon;
	private PreferenceScreen PrefScreen;
	private LinearLayout mDownloadButtonsLayout;
	private LinearLayout mDownloadProgressLayout;
	private Handler mUpdateNotiHandler;
	private String mRomListRateCount;
	private String mRomListRating;
	private long mRomListDownloads;
	private PreferenceCategory CommentCat;
	private boolean mWebpageAdded;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getSupportMenuInflater().inflate(R.menu.share_action_provider, menu);

		MenuItem actionItem = menu.findItem(R.id.menu_share);
		ShareActionProvider actionProvider = (ShareActionProvider) actionItem
				.getActionProvider();
		actionProvider.setShareIntent(createShareIntent());

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = NavUtils.getParentActivityIntent(this);
			intent.putExtra("jsonurl", mROMListURL);
			intent.putExtra("description", mROMListDescription);
			intent.putExtra("iconurl", mIconURL);
			intent.putExtra("devname", mDev);
			intent.putExtra("romname", mROMListRomName);
			intent.putExtra("downloads", mRomListDownloads);
			intent.putExtra("rateCount", mRomListRateCount);
			intent.putExtra("rating", mRomListRating);
			intent.putExtra("webpage", mWebpage);

			NavUtils.navigateUpTo(this, intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onDestroy() {

		isDestroyed = true;
		mWebpageAdded = false;

		EasyTracker.getInstance().activityStop(this);

		super.onDestroy();
	}

	private Runnable updateNotification = new Runnable() {
		public void run() {

			if (DownloadFileService.getRomID() == mID) {
				if (mDownloadButtonsLayout != null) {
					mDownloadButtonsLayout.setVisibility(LinearLayout.GONE);
				}
				if (mDownloadProgressLayout != null) {
					mDownloadProgressLayout.setVisibility(LinearLayout.VISIBLE);
				}
			} else {
				if (mDownloadButtonsLayout != null) {
					mDownloadButtonsLayout.setVisibility(LinearLayout.VISIBLE);
				}
				if (mDownloadProgressLayout != null) {
					mDownloadProgressLayout.setVisibility(LinearLayout.GONE);
				}
			}
			if (!isDestroyed) {
				mUpdateNotiHandler.postDelayed(this, 2000);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setTheme(Theme.getTheme(this));

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_download);

		EasyTracker.getInstance().activityStart(this);

		setSupportProgressBarIndeterminateVisibility(false);

		mContext = this;

		isDestroyed = false;

		final Bundle Extras = getIntent().getExtras();

		if (Extras == null) {
			finish();
			return;
		}

		mName = Extras.getString("romname");

		mFilename = Extras.getString("filename");

		mURL = Extras.getString("url");

		mID = Integer.valueOf(Extras.getString("id"));

		mDownloads = Extras.getString("downloads");

		mRateCount = Extras.getString("rateCount");

		mRating = Extras.getString("rating");
		
		mWebpage = Extras.getString("webpage");
		mWebpageAdded = false;

		if (mURL.contains("goo.im")) {
			isNonDirectLink = true;
		} else if (mURL.contains("gigashare.")) {
			isNonDirectLink = true;
		} else {
			isNonDirectLink = false;
		}

		mIconURL = Extras.getString("icon");
		mDev = Extras.getString("dev");
		mMD5 = Extras.getString("md5");

		mSummary = Extras.getString("summary");

		mROMListDescription = Extras.getString("description");
		mROMListURL = Extras.getString("romlisturl");
		mROMListRomName = Extras.getString("romlistromname");
		mRomListRateCount = Extras.getString("romlistrateCount");
		mRomListRating = Extras.getString("romlistrating");
		mRomListDownloads = Extras.getLong("romlistdownloads");

		TextView TvName = (TextView) findViewById(R.id.name);
		TvName.setSelected(true);
		TvName.setText(mName);

		TextView TvDev = (TextView) findViewById(R.id.devname);
		TvDev.setText(mDev);

		mDownloadButtonsLayout = (LinearLayout) findViewById(R.id.download_buttons);
		mDownloadProgressLayout = (LinearLayout) findViewById(R.id.download_running);

		if (DownloadFileService.getRomID() == mID) {
			mDownloadButtonsLayout.setVisibility(LinearLayout.GONE);
			mDownloadProgressLayout.setVisibility(LinearLayout.VISIBLE);
		}

		final Button cancelBt = (Button) findViewById(R.id.cancelBt);
		cancelBt.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				mDownloadButtonsLayout.setVisibility(LinearLayout.VISIBLE);
				mDownloadProgressLayout.setVisibility(LinearLayout.GONE);

				DownloadFileService.cancel(mID);
			}
		});

		final Button viewLinkBt = (Button) findViewById(R.id.viewLinkBt);
		viewLinkBt.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				Uri viewLink = Uri.parse(mURL);
				Intent a = new Intent("android.intent.action.VIEW")
						.setData(viewLink);

				startActivity(a);
			}
		});

		final Button viewLinkBt2 = (Button) findViewById(R.id.viewLinkBt2);
		viewLinkBt2.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				Uri viewLink = Uri.parse(mURL);
				Intent a = new Intent("android.intent.action.VIEW")
						.setData(viewLink);

				startActivity(a);
			}
		});

		final Button downloadBt = (Button) findViewById(R.id.downloadBt);
		downloadBt.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				if (isNonDirectLink) {

					Intent intent = new Intent(mContext,
							GooDownloadActivity.class);
					intent.putExtra("url", mURL);
					intent.putExtra("filename", mFilename);
					intent.putExtra("md5", mMD5);
					intent.putExtra("id", mID);
					intent.putExtra("icon", mRomIcon);
					intent.putExtra("extras", Extras);

					startActivity(intent);

				}

				else {

					mDownloadButtonsLayout.setVisibility(LinearLayout.GONE);
					mDownloadProgressLayout.setVisibility(LinearLayout.VISIBLE);

					Download download = new Download(mContext);
					download.setFilename(mFilename);
					download.setMD5(mMD5);
					download.setURL(mURL);
					download.setRomID(mID);
					download.setIcon(mRomIcon);
					download.setExtras(Extras);
					download.startDownload();

				}

				String increase_download = "http://www.2-si.net/_roms/?do=download&id="
						+ mID;
				new AsyncTask<String, String, String>() {

					@Override
					protected String doInBackground(String... arg0) {

						try {
							URL readJSON = new URL(arg0[0]);
							URLConnection tc = readJSON.openConnection();
							tc.getContent();

						} catch (MalformedURLException e) {
						} catch (IOException e) {
						}
						return null;
					}

				}.execute(increase_download);

			};
		});

		PrefScreen = getPreferenceManager().createPreferenceScreen(this);

		String md5sum = "\n" + getText(R.string.md5_hash) + ": "
				+ getText(R.string.copy_md5) + " " + mMD5;
		hasMD5 = true;
		if (MadUtils.isEmpty(mMD5)) {
			md5sum = "";
			hasMD5 = false;
		}

		String summary = "\n\n" + mSummary + "\n";
		if (MadUtils.isEmpty(mSummary)) {
			summary = "\n";
		}

		PrefROMStats Description = new PrefROMStats(mContext);
		Description.setTitle(getText(R.string.DESCRIPTION));
		Description.setSummary(mName + summary + "\n"
				+ getText(R.string.file_o) + ": " + mFilename + md5sum);
		Description.setEnabled(true);
		Description.setSelectable(false);
		if (hasMD5) {
			Description.setMD5Link(true, mMD5);
		}
		Description.setLinkify(true);
		Description.setDownloads(mDownloads);
		Description.setRateCount(mRateCount);
		Description.setRating(mRating);
		PrefScreen.addPreference(Description);

		setPreferenceScreen(PrefScreen);

		setSupportProgressBarIndeterminateVisibility(true);

		new LoadICON().execute();

		// LOAD CHANGELOG
		new AsyncTask<String, String, String>() {

			@Override
			protected String doInBackground(String... arg0) {

				String changelog = getText(R.string.no_changelog_found)
						.toString();

				try {

					String url = "http://2-si.net/_roms/?do=json&sa=chglog&id=";
					URL readJSON = new URL(url + mID);
					URLConnection tc = readJSON.openConnection();
					BufferedReader in = new BufferedReader(
							new InputStreamReader(tc.getInputStream()));

					String line;

					while ((line = in.readLine()) != null) {

						JSONObject jo = new JSONObject(line);

						/*
						 * if ((jo.has("chglog")) &&
						 * (!jo.getString("chglog").equals(""))) { String
						 * getChanglog = jo.getString("chglog");
						 * 
						 * 
						 * changelog = getChanglog );
						 * 
						 * }
						 */
						if (jo.has("chglog")
								&& !MadUtils.isEmpty(jo.getString("chglog"))) {

							char bullet = '\u2022';
							changelog = jo.getString("chglog")
									.replace("\n", "<br>")
									.replace("[*]", bullet + " ");

						}

					}
				} catch (MalformedURLException e) {
					String error = getText(R.string.error_loading_changelog)
							.toString();
					return error;
				} catch (IOException e) {
					String error = getText(R.string.error_loading_changelog)
							.toString();
					return error;
				} catch (JSONException e) {
					String error = getText(R.string.no_changelog_found)
							.toString();
					return error;
				}

				return changelog;

			}

			@Override
			protected void onPostExecute(String result) {

				Preference ChangeLog = new Preference(mContext);
				ChangeLog.setTitle(getText(R.string.CHANGELOG));
				ChangeLog.setSummary(Html.fromHtml(result));
				ChangeLog
						.setLayoutResource(R.layout.pref_download_data_changelog);
				ChangeLog.setSelectable(false);
				ChangeLog.setEnabled(true);
				PrefScreen.addPreference(ChangeLog);

				// DO COMMENT
				Preference DoComment = new Preference(mContext);
				DoComment.setTitle(getText(R.string.RATE_AND_COMMENT));
				DoComment.setLayoutResource(R.layout.pref_download_rate);
				DoComment.setEnabled(true);
				DoComment
						.setOnPreferenceClickListener(new OnPreferenceClickListener() {

							public boolean onPreferenceClick(Preference arg0) {
								doComment();
								return false;
							}
						});

				PrefScreen.addPreference(DoComment);

				CommentCat = new PreferenceCategory(mContext);
				CommentCat.setTitle(getText(R.string.COMMENTS));
				CommentCat.setLayoutResource(R.layout.pref_download_cat_sep);
				PrefScreen.addPreference(CommentCat);

				new LoadComments().execute();

			}

		}.execute();

		mUpdateNotiHandler = new Handler();
		mUpdateNotiHandler.post(updateNotification);

	}

	private Intent createShareIntent() {

		Intent share = new Intent("android.intent.action.SEND")
				.setType("text/plain")
				.putExtra("android.intent.extra.SUBJECT",
						mName + " - " + getText(R.string.app_name))
				.putExtra("android.intent.extra.TEXT", mURL);
		return share;
	}

	public void doComment() {
		new DoComment(mContext, mID, new LoadComments());
	}

	public class LoadComments extends AsyncTask<String, String, String> {

		boolean isLight;

		@Override
		protected void onPreExecute() {
			isLight = Theme.isThemeLight(mContext);
		}

		@Override
		protected String doInBackground(String... arg0) {

			try {
				String url = "http://2-si.net/_roms/?do=json&sa=comments&id=";
				URL readJSON = new URL(url + mID);
				URLConnection tc = readJSON.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						tc.getInputStream()));

				String line = in.readLine();

				if (line == null) {
					return "error";
				} else {
					return line;
				}
			} catch (IOException e) {
				String error = "error";
				return error;
			}

		}

		@Override
		protected void onPostExecute(String result) {

			CommentCat.removeAll();

			if (result.equals("error")) {

				Preference Comments = new Preference(mContext);
				Comments.setSummary(getText(R.string.error_loading_comments));
				Comments.setLayoutResource(R.layout.pref_download_data_comments);
				Comments.setSelectable(false);
				Comments.setEnabled(true);
				CommentCat.addPreference(Comments);

			}

			else {

				try {

					int no = 0;

					JSONObject jObjectMain = new JSONObject(result);
					JSONArray jArrayList = jObjectMain.getJSONArray("list");

					no = jArrayList.length();

					int end = 5;
					if (end > jArrayList.length()) {
						end = jArrayList.length();
					}
					for (int j = 0; j < end; j++) {
						JSONObject jo = (JSONObject) jArrayList.get(j);

						if (!MadUtils.isEmpty(jo.getString("comment"))
								&& !jo.getString("comment").equals(" ")) {

							PrefComment comment = new PrefComment(mContext);
							comment.setTitle(jo.getString("name"));
							comment.setSummary(jo.getString("comment")
									.replace(":br:", " ").replace("  ", " ")
									.replace("   ", " "));
							comment.setRating(jo.getString("rating"));
							comment.setEnabled(true);
							comment.setLightTheme(isLight);
							comment.setTimestamp(jo.getString("timestamp"));
							CommentCat.addPreference(comment);

						}

					}

					if (no > 5) {
						Preference viewall = new Preference(mContext);
						viewall.setSummary(getText(R.string.SEE_ALL));
						viewall.setLayoutResource(R.layout.pref_download_viewall);

						viewall.setOnPreferenceClickListener(new OnPreferenceClickListener() {

							public boolean onPreferenceClick(Preference arg0) {

								Intent intent = new Intent(mContext,
										AllComments.class).putExtra("id", mID
										+ "");
								;
								startActivity(intent);
								return false;
							}
						});

						CommentCat.addPreference(viewall);
					}
				} catch (JSONException e) {
					Preference Comments = new Preference(mContext);
					Comments.setSummary(getText(R.string.no_comments));
					Comments.setLayoutResource(R.layout.pref_download_data_comments);
					Comments.setSelectable(false);
					Comments.setEnabled(true);
					CommentCat.addPreference(Comments);

				}

			}

			// ADD WEBPAGE
			addWebpage();

			setSupportProgressBarIndeterminateVisibility(false);

		}

	};
	
	private void addWebpage() {
		
		if (mWebpageAdded) {
			return;
		}

		if (!MadUtils.isEmpty(mWebpage)) {

			Uri uri = null;
			try {
				uri = Uri.parse(mWebpage);
			} catch (NullPointerException e) {
				uri = null;
			}

			if (uri != null) {

				PreferenceCategory Seperator = new PreferenceCategory(
						mContext);
				Seperator
						.setLayoutResource(R.layout.pref_download_seperator);
				PrefScreen.addPreference(Seperator);

				PreferenceCategory Developer = new PreferenceCategory(
						mContext);
				Developer.setTitle(getText(R.string.DEVELOPER));
				Developer.setLayoutResource(R.layout.pref_download_cat_sep);
				PrefScreen.addPreference(Developer);

				PrefWebpage webpage = new PrefWebpage(mContext);
				webpage.setTitle(getText(R.string.visit_webpage));
				webpage.setURL(mWebpage, uri);
				Developer.addPreference(webpage);

				
			}
		}
		mWebpageAdded = true;
	}

	public class LoadICON extends AsyncTask<Void, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(Void... arg) {

			if (!MadUtils.isEmpty(mIconURL)) {

				mIcon = (ImageView) findViewById(R.id.romicon);

				BitmapFactory.Options bmOptions;
				bmOptions = new BitmapFactory.Options();
				bmOptions.inSampleSize = 1;
				mRomIcon = LoadImage(mIconURL, bmOptions);

				return mRomIcon;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap bm) {

			if (bm != null) {
				Bitmap scaled = new BlurImage().createResizedBitmap(
						getResources(), bm, 60, 60, true);

				mIcon.setImageBitmap(scaled);
			}
		}

		private Bitmap LoadImage(String URL, BitmapFactory.Options options) {
			Bitmap bitmap = null;
			InputStream in = null;
			try {

				in = OpenHttpConnection(URL);
				if (in != null) {
					bitmap = BitmapFactory.decodeStream(in, null, options);
					in.close();

				}
			} catch (IOException e1) {
				return null;
			}
			return bitmap;
		}

		private InputStream OpenHttpConnection(String strURL)
				throws IOException {
			InputStream inputStream = null;
			URL url = new URL(strURL);
			URLConnection conn = url.openConnection();

			try {
				HttpURLConnection httpConn = (HttpURLConnection) conn;
				httpConn.setRequestMethod("GET");
				httpConn.connect();

				inputStream = httpConn.getInputStream();

			} catch (Exception ex) {
				return null;
			}
			return inputStream;
		}

	}

}