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
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class SearchActivity extends SherlockPreferenceActivity {

	public static Context c;

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

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);

		setSupportProgressBarIndeterminateVisibility(false);

		SearchActivity.c = this;

		Intent intent = getIntent();

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);

			String Search = "http://2-si.net/_roms/?do=json&sa=search&keywords="
					+ query;

			new SearchAsync().execute(Search, query);

		}

	}

	public class SearchAsync extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {

			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected String doInBackground(String... arg) {
			PreferenceScreen PrefScreen = getPreferenceManager()
					.createPreferenceScreen(SearchActivity.c);

			try {
				URL readJSON = new URL(arg[0]);
				URLConnection tc = readJSON.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						tc.getInputStream()));

				String line;

				while ((line = in.readLine()) != null) {

					JSONObject jObjectMain = new JSONObject(line);
					JSONArray jArrayList = jObjectMain.getJSONArray("list");

					PreferenceCategory PrefCat = new PreferenceCategory(
							SearchActivity.c);
					PrefCat.setTitle("RESULTS FOR - " + arg[1]);
					PrefScreen.addPreference(PrefCat);

					for (int j = 0; j < jArrayList.length(); j++) {
						JSONObject jo = (JSONObject) jArrayList.get(j);

						PrefImage ROMs = new PrefImage(SearchActivity.c);
						ROMs.setTitle(jo.getString("name"));
						ROMs.setIcon(jo.getString("icon"));
						ROMs.setEnabled(true);
						ROMs.setKey(jo.getString("codeName"));
						ROMs.setDev(jo.getString("dev"));
						ROMs.setSummary(jo.getString("summary"));
						PrefCat.addPreference(ROMs);

					}

				}

			} catch (MalformedURLException e) {
				String error = "Unable to connect. Please try again.";
				return error;
			} catch (IOException e) {
				String error = "Unable to connect. Please try again.";
				return error;
			} catch (JSONException e) {
				String error = "No results found.";
				return error;
			}
			setPreferenceScreen(PrefScreen);
			return null;
		}

		@Override
		protected void onPostExecute(String result) {

			if (result != null) {
				Toast.makeText(SearchActivity.c, result, Toast.LENGTH_SHORT)
						.show();
			}
			setSupportProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			final Preference preference) {

		String str1 = "http://www.2-si.net/_roms/?do=json&sa=rom&rom=";
		String str2 = preference.getKey();
		String jsonUrl = str1 + str2;
		Intent NewThis = new Intent(this, ReadROMFileList.class)
				.putExtra("jsonurl", jsonUrl)
				.putExtra("description", preference.getSummary());
		startActivity(NewThis);

		return false;
	}

}