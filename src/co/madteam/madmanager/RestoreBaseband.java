package co.madteam.madmanager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import co.madteam.madmanager.utilities.MadUtils;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.analytics.tracking.android.EasyTracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RestoreBaseband extends SherlockPreferenceActivity {

	private Context c;
	private PreferenceScreen PrefScreen;
	private File directory;

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
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(getText(R.string.new_backup))
				.setIcon(Theme.isActionBarLight(getApplicationContext()) ? R.drawable.action_content_new_light
								: R.drawable.action_content_new)
				.setOnMenuItemClickListener(new BackupBaseband(this))
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setTheme(Theme.getTheme(this));

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.pref_screen_restore);

		EasyTracker.getInstance().activityStart(this);

		setSupportProgressBarIndeterminateVisibility(false);

		c = this;

		directory = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/madmanager/backup");

		loadBackups();
	}

	public void loadBackups() {

		PrefScreen = getPreferenceManager().createPreferenceScreen(this);

		String state = Environment.getExternalStorageState();

		if (!Environment.MEDIA_MOUNTED.equals(state)) {

			ListView list = (ListView) findViewById(android.R.id.list);
			list.setVisibility(ListView.GONE);
			TextView tv = (TextView) findViewById(R.id.nobackups);
			tv.setText(getText(R.string.sdcard_not_mounted));
			tv.setVisibility(TextView.VISIBLE);

			return;
		}

		if (!directory.exists()) {
			directory.mkdirs();
		}

		if (directory.canRead()) {
			File[] files = directory.listFiles();

			if (files.length == 0) {
				ListView list = (ListView) findViewById(android.R.id.list);
				list.setVisibility(ListView.GONE);
				TextView tv = (TextView) findViewById(R.id.nobackups);
				tv.setText(R.string.nobackups);
				tv.setVisibility(TextView.VISIBLE);

			} else {
				for (File file : files) {
					if (file.isDirectory()) {

						Preference Backups = new Preference(this);
						Backups.setTitle(file.getName());
						Backups.setSummary(summary(file));
						Backups.setEnabled(true);
						Backups.setLayoutResource(R.layout.pref);
						PrefScreen.addPreference(Backups);

					}
				}
			}

		}

		setPreferenceScreen(PrefScreen);

	}

	private String summary(File file) {

		long lastModified = file.lastModified();
		long currentMilli = System.currentTimeMillis();

		long mins = (currentMilli - lastModified) / (1000 * 60);

		long hours = (currentMilli - lastModified) / (1000 * 60 * 60);

		long days = (currentMilli - lastModified) / (1000 * 60 * 60 * 24);

		while (hours > 24) {
			hours = hours - 24;
		}

		while (mins > 60) {
			mins = mins - 60;
		}

		String Days = getText(R.string.days).toString();
		String Hours = getText(R.string.hours).toString();
		String Mins = getText(R.string.mins).toString();
		String ago = " " + getText(R.string.ago).toString();

		if (days <= 1) {
			Days = getText(R.string.day).toString();
		}

		if (hours <= 1) {
			Hours = getText(R.string.hour).toString();
		}

		if (mins <= 1) {
			Mins = getText(R.string.min).toString();
		}

		String summary = days + " " + Days + ", " + hours + " " + Hours + ", "
				+ mins + " " + Mins + ago;

		if (days <= 0) {
			summary = hours + " " + Hours + ", " + mins + " " + Mins + ago;
		}

		if (hours <= 0 && days <= 0) {
			summary = mins + " " + Mins + ago;
		}

		return summary;

	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			final Preference preference) {

		CharSequence[] rord = { getText(R.string.restore),
				getText(R.string.delete) };

		AlertDialog.Builder choose = new AlertDialog.Builder(this);
		choose.setTitle(preference.getTitle());
		choose.setItems(rord, new OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				if (arg1 == 0) {

					AlertDialog.Builder builder = new AlertDialog.Builder(c);
					builder.setTitle(
							getText(R.string.restore) + " - "
									+ preference.getTitle())
							.setMessage(
									getText(R.string.restore_baseband_confirm))
							.setPositiveButton(getText(R.string.restore),
									new OnClickListener() {

										public void onClick(
												final DialogInterface dialog,
												final int which) {

											new AsyncTask<String, String, String>() {
												ProgressDialog mProgressDialog;

												@Override
												protected void onPreExecute() {

													mProgressDialog = new ProgressDialog(
															c);
													mProgressDialog
															.setIndeterminate(false);
													mProgressDialog.setMessage(c
															.getText(R.string.preparing));
													mProgressDialog
															.setProgressStyle(ProgressDialog.STYLE_SPINNER);
													mProgressDialog
															.setCancelable(false);
													mProgressDialog.show();
												}

												@Override
												protected String doInBackground(
														String... arg0) {

													publishProgress(c
															.getText(
																	R.string.verifying_md5)
															.toString());

													String dir = Environment
															.getExternalStorageDirectory()
															.getPath()
															+ "/madmanager/backup/"
															+ preference
																	.getTitle();

													File baseband = new File(
															dir + "/baseband");
													if (baseband.length() == 0) {
														return getText(
																R.string.md5_mismatch)
																.toString();
													}

													String file_md5 = null;

													try {
														FileInputStream fis = new FileInputStream(
																dir + "/md5");
														DataInputStream dis = new DataInputStream(
																fis);
														BufferedReader in = new BufferedReader(
																new InputStreamReader(
																		dis));
														file_md5 = in
																.readLine();
													} catch (Exception e) {
														return getText(
																R.string.md5_mismatch)
																.toString();
													}

													if (MadUtils
															.isEmpty(file_md5)) {
														return getText(
																R.string.md5_mismatch)
																.toString();
													} else if (!file_md5
															.contains(MD5Checksum
																	.getMD5Checksum(dir
																			+ "/baseband"))) {
														return getText(
																R.string.md5_mismatch)
																.toString();
													}

													int DeviceID = Devices
															.getID();

													switch (DeviceID) {

													case Devices.GALAXY5:

														buildScriptGalaxy5(dir);
														break;

													case Devices.GALAXY_MINI:
														buildScriptGalaxy5(dir);

														break;
													}

													return null;

												}

												@Override
												protected void onProgressUpdate(
														String... progress) {
													mProgressDialog
															.setMessage(progress[0]);
												}

												@Override
												protected void onPostExecute(
														String result) {

													if (!MadUtils
															.isEmpty(result)) {
														Toast.makeText(
																c,
																result,
																Toast.LENGTH_SHORT)
																.show();
													}
													mProgressDialog.cancel();

												}

												@SuppressLint("SdCardPath")
												private void buildScriptGalaxy5(
														String dir) {

													dir = dir
															.replace(
																	Environment
																			.getExternalStorageDirectory()
																			.getPath(),
																	"/sdcard")
															.replace("/mnt", "");
													publishProgress(c
															.getText(
																	R.string.copying_needed_files)
															.toString());

													Assets.copyFile(c,
															"redbend_ua");

													String[] copyfiles = new String[4];

													copyfiles[0] = "rm -rf /cache/recovery;";
													copyfiles[1] = "mkdir /cache/recovery;";
													copyfiles[2] = "cat /data/data/co.madteam.madmanager/files/redbend_ua > '/cache/recovery/redbend_ua';";
													copyfiles[3] = "chmod 777 /cache/recovery/redbend_ua;";

													Command.s(copyfiles);

													String restore_file = "/cache/recovery/redbend_ua restore "
															+ dir
															+ "/baseband"
															+ " /dev/block/bml4";

													publishProgress(c
															.getText(
																	R.string.generating_command)
															.toString());

													String[] script = new String[12];

													script[0] = "echo 'ui_print(\" \");' > /cache/recovery/extendedcommand;";
													script[1] = "echo 'ui_print(\""
															+ Command
																	.appSign(c)
															+ "\");' >> /cache/recovery/extendedcommand;";
													script[2] = "echo 'ui_print(\" \");' >> /cache/recovery/extendedcommand;";
													script[3] = "echo 'ui_print(\"Restoring baseband...\");' >> /cache/recovery/extendedcommand;";
													script[4] = "echo 'run_program(\"/cache/recovery/madmanager\");' >> /cache/recovery/extendedcommand;";
													script[5] = "echo '#!/sbin/busybox sh' > /cache/recovery/madmanager;";
													script[6] = "echo '"
															+ restore_file
															+ "' >> /cache/recovery/madmanager;";
													script[7] = "echo 'rm /cache/recovery/madmanager' >> /cache/recovery/madmanager;";
													script[8] = "echo 'rm /cache/recovery/redbend_ua' >> /cache/recovery/madmanager;";
													script[9] = "echo 'reboot' >> /cache/recovery/madmanager;";
													script[10] = "chmod 777 /cache/recovery/madmanager;";
													script[11] = "chmod 777 /cache/recovery/extendedcommand;";

													Command.s(script);
													publishProgress(c
															.getText(
																	R.string.rebooting_into_recovery)
															.toString());
													Command.line("reboot recovery");
													Command.reboot(c, "recovery");

												}

											}.execute();

										}
									})
							.setNegativeButton(getText(R.string.cancel), null)
							.show();
				}

				if (arg1 == 1) {
					AlertDialog.Builder builder = new AlertDialog.Builder(c);
					builder.setTitle(getText(R.string.delete) + " - "
							+ preference.getTitle());
					builder.setMessage(getText(R.string.delete_backup_confirm))
							.setPositiveButton(getText(R.string.delete),
									new OnClickListener() {

										public void onClick(
												final DialogInterface dialog,
												final int which) {

											new Delete().execute(directory
													.getAbsolutePath()
													+ "/"
													+ preference.getTitle());

										}
									})
							.setNegativeButton(getText(R.string.cancel), null)
							.show();
				}
			}

		});
		choose.setNegativeButton(getText(R.string.cancel), null).show();

		return false;
	}

	public class Delete extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected String doInBackground(String... arg) {

			File dir = new File(arg[0]);
			if (dir.isDirectory()) {
				for (File file : dir.listFiles()) {
					file.delete();
				}
				dir.delete();
			}

			publishProgress();
			return null;
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			setSupportProgressBarIndeterminateVisibility(false);
			loadBackups();

		}
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

}