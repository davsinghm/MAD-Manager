package co.madteam.madmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.widget.ListView;
import android.widget.TextView;

public class AllComments extends SherlockPreferenceActivity {

	PreferenceScreen PrefScreen;
	String mID = "";
	Context mContext;

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

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.pref_screen_nopad);

		EasyTracker.getInstance().activityStart(this);

		mID = getIntent().getStringExtra("id");

		mContext = this;

		PrefScreen = getPreferenceManager().createPreferenceScreen(this);

		setPreferenceScreen(PrefScreen);

		loadComments();

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
	}

	private void loadComments() {
		new AsyncTask<String, String, String>() {

			boolean isLight;

			@Override
			protected void onPreExecute() {

				isLight = Theme.isThemeLight(mContext);
				displayError(false);
				PrefScreen.removeAll();

			}

			@Override
			protected String doInBackground(String... arg0) {

				if (!Command.isNetworkAvailable(mContext)) {
					return "no_connection";
				}

				try {

					String url = "http://2-si.net/_roms/?do=json&sa=comments&id=";
					URL readJSON = new URL(url + mID);
					URLConnection tc = readJSON.openConnection();
					BufferedReader in = new BufferedReader(
							new InputStreamReader(tc.getInputStream()));

					String line = in.readLine();

					if (line == null) {
						return "error";
					} else {
						return line;
					}
				} catch (MalformedURLException e) {
					String error = "error";
					return error;
				} catch (IOException e) {
					String error = "error";
					return error;
				}

			}

			@Override
			protected void onPostExecute(String result) {

				if (result.equals("error")) {

					setError(getText(R.string.network_error).toString());
					displayError(true);

				} else if (result.equals("no_connection")) {

					setError(getText(R.string.no_connection).toString());
					displayError(true);

				}

				else {

					try {

						JSONObject jObjectMain = new JSONObject(result);
						JSONArray jArrayList = jObjectMain.getJSONArray("list");

						for (int j = 0; j < jArrayList.length(); j++) {
							JSONObject jo = (JSONObject) jArrayList.get(j);

							if (!MadUtils.isEmpty(jo.getString("comment"))
									&& !jo.getString("comment").equals(" ")) {

								PrefComment comment = new PrefComment(mContext);
								comment.setTitle(jo.getString("name"));
								comment.setSummary(jo.getString("comment")
										.replace(":br:", " ")
										.replace("  ", " ").replace("   ", " "));
								comment.setRating(jo.getString("rating"));
								comment.setLightTheme(isLight);
								comment.setEnabled(true);
								comment.setTimestamp(jo.getString("timestamp"));
								comment.setLayoutResource(R.layout.pref_download_comment_nobg);
								PrefScreen.addPreference(comment);

							}
						}

					} catch (JSONException e) {

						setError(getText(R.string.network_error).toString());
						displayError(true);

					}

				}

				setSupportProgressBarIndeterminateVisibility(false);

			}

		}.execute();
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

}