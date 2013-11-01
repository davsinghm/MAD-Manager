package co.madteam.madmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

public class FixPermissions extends SherlockActivity {

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

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fixpermissions);

		final TextView fix_tv = (TextView) findViewById(R.id.fixpermissions_tv);

		new AsyncTask<String, String, String>() {

			@Override
			protected String doInBackground(String... string) {

				Runtime runtime = Runtime.getRuntime();
				Process proc = null;
				OutputStreamWriter osw = null;
				try {
					proc = runtime.exec("su");
					osw = new OutputStreamWriter(proc.getOutputStream());
					osw.write("fix_permissions");
					osw.flush();
					osw.close();


					proc.waitFor();

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(proc.getInputStream()));
					int read;
					char[] buffer = new char[2];
					StringBuffer output = new StringBuffer();

					while ((read = reader.read(buffer)) > 0) {
						output.append(buffer, 0, read);
						publishProgress(output.toString());

					}

				} catch (InterruptedException e) {
				} catch (IOException e) {
				}

				return null;

			}

			@Override
			protected void onProgressUpdate(String... string) {
				fix_tv.setText(string[0]);

			}

		}.execute();

	}

}