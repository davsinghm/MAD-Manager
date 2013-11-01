package co.madteam.madmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.analytics.tracking.android.EasyTracker;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ReadROMList extends SherlockPreferenceActivity {

	private Context mContext;
	private String mURL;
	private PreferenceScreen PrefScreen;

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

		setContentView(R.layout.pref_screen_romlist);

		EasyTracker.getInstance().activityStart(this);

		String device = Devices.getDeviceName();

		mURL = "http://www.2-si.net/_roms/?do=json&sa=device&device=" + device;

		mContext = this;

		Button try_again = (Button) findViewById(R.id.button);
		try_again.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				loadFileList(mURL);
			}
		});

		loadFileList(mURL);
	}

	private void setMessage(String error) {
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
	
	private void displayMessage() {
		ListView listView = (ListView) findViewById(android.R.id.list);
		listView.setVisibility(ListView.GONE);
		TextView textView = (TextView) findViewById(R.id.msg);
		textView.setVisibility(TextView.VISIBLE);
		Button button = (Button) findViewById(R.id.button);
		button.setVisibility(Button.GONE);
	}

	private void loadFileList(String url) {
		displayError(false);

		new AsyncTask<String, String, Integer>() {

			@Override
			protected void onPreExecute() {

				setSupportProgressBarIndeterminateVisibility(true);
			}

			@Override
			protected Integer doInBackground(String... arg) {

				if (!Command.isNetworkAvailable(mContext)) {
					return 2;
				}

				PrefScreen = getPreferenceManager()
						.createPreferenceScreen(mContext);

				try {
					URL readJSON = new URL(mURL);
					URLConnection tc = readJSON.openConnection();
					BufferedReader in = new BufferedReader(
							new InputStreamReader(tc.getInputStream()));

					String line;

					while ((line = in.readLine()) != null) {

						JSONObject jObjectMain = new JSONObject(line);
						JSONArray jArrayList = jObjectMain.getJSONArray("list");

						for (int j = 0; j < jArrayList.length(); j++) {
							JSONObject jo = (JSONObject) jArrayList.get(j);

							PrefImage ROMs = new PrefImage(mContext);
							ROMs.setTitle(jo.getString("name"));
							ROMs.setIconURL(jo.getString("icon"));
							ROMs.setEnabled(true);
							ROMs.setRomName(jo.getString("codeName"));
							ROMs.setSummary(jo.getString("dev"));
							ROMs.setRating(jo.getString("rating"));
							ROMs.setRateCount(jo.getString("rateCount"));
							ROMs.setDescription(jo.getString("summary"));
							ROMs.setDownloads(jo.getLong("downloads"));
							ROMs.setWebpage(jo.getString("webpage_url"));

							PrefScreen.addPreference(ROMs);

						}

					}

				} catch (IOException e) {
					return 3;
				} catch (JSONException e) {
					return 1;
				}

				setPreferenceScreen(PrefScreen);
				return 0;
			}

			@Override
			protected void onPostExecute(Integer result) {
				
				switch (result) {
				case 1: 
					setMessage(getText(R.string.no_roms_found).toString());
					displayMessage();
					break;
				case 2: 
					setMessage(getText(R.string.no_connection).toString());
					displayError(true);
					break;
				case 3: 
					setMessage(getText(R.string.network_error).toString());
					displayError(true);
					break;
				}

				setSupportProgressBarIndeterminateVisibility(false);
			}
		}.execute();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			final Preference preference) {
		
		PrefImage prefImage = (PrefImage) PrefScreen.getPreference(preference.getOrder());

		String jsonUrl = prefImage.getRomName();
		Intent intent = new Intent(this, ReadROMFileList.class);
		intent.putExtra("jsonurl", jsonUrl);
		intent.putExtra("description", prefImage.getDescription());
		intent.putExtra("iconurl", prefImage.getIconURL());
		intent.putExtra("devname", prefImage.getSummary());
		intent.putExtra("romname", prefImage.getTitle());
		intent.putExtra("downloads", prefImage.getDownloads());
		intent.putExtra("rateCount", prefImage.getRateCount());
		intent.putExtra("rating", prefImage.getRating());
		intent.putExtra("webpage", prefImage.getWebpage());

		startActivity(intent);

		return false;
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
}