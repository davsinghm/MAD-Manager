package co.madteam.madmanager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import co.madteam.madmanager.utilities.MadEnvironment;
import co.madteam.madmanager.utilities.MadUtils;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Restore extends SherlockPreferenceActivity {

	private Context mContext;
	private PreferenceScreen PrefScreen;
	private File mDirectory;
	private File mDirectoryExt;

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
				.setIcon(
						Theme.isActionBarLight(getApplicationContext()) ? R.drawable.action_content_new_light
								: R.drawable.action_content_new)
				.setOnMenuItemClickListener(new Backup(mContext))
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

		setSupportProgressBarIndeterminateVisibility(false);

		mContext = this;

		if (MadEnvironment.isExternalSDCardMounted()) {
		mDirectoryExt = new File(MadEnvironment.getExternalSDCardPath()
				+ "/clockworkmod/backup");
		}
		mDirectory = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/clockworkmod/backup");

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

		if (!mDirectory.exists()) {
			mDirectory.mkdirs();
		}

		if (mDirectory.canRead()) {
			File[] files = mDirectory.listFiles();

			String location = "EXT.SD";
			if (MadEnvironment.isUSBStorageExist()) {
				location = "INT.SD";
			}
			for (File file : files) {
				if (file.isDirectory()) {

					String format = getFormat(file);

					String info = format + "   " + location;
					if (format == null) {
						info = location;
					}

					PrefBackup Backup = new PrefBackup(this);
					Backup.setTitle(file.getName());
					Backup.setSummary(summary(file));
					Backup.setEnabled(true);
					Backup.setSize(getSize(file));
					Backup.setInfo(info);
					Backup.setKey("internal");
					PrefScreen.addPreference(Backup);

				}
			}
		}

		if (mDirectoryExt != null) {

			if (!mDirectoryExt.exists()) {
				mDirectoryExt.mkdirs();
			}

			if (mDirectoryExt.canRead()) {
				File[] files = mDirectoryExt.listFiles();

				for (File file : files) {

					if (file.isDirectory()) {

						String format = getFormat(file);
						String location = "EXT.SD";

						String info = format + "   " + location;
						if (format == null) {
							info = location;
						}

						PrefBackup Backup = new PrefBackup(this);
						Backup.setTitle(file.getName());
						Backup.setSummary(summary(file));
						Backup.setInfo(info);
						Backup.setSize(getSize(file));
						Backup.setEnabled(true);
						Backup.setKey("external");
						PrefScreen.addPreference(Backup);

					}
				}
			}
		}

		setPreferenceScreen(PrefScreen);

		if (PrefScreen.getPreferenceCount() == 0) {
			ListView list = (ListView) findViewById(android.R.id.list);
			list.setVisibility(ListView.GONE);
			TextView tv = (TextView) findViewById(R.id.nobackups);
			tv.setText(R.string.nobackups);
			tv.setVisibility(TextView.VISIBLE);

		}

	}

	private long getDirSize(File dir) {
		long size = 0;
		File[] subFiles = dir.listFiles();
		for (File file : subFiles) {
			if (file.isFile()) {
				size += file.length();
			}
		}
		return size;
	}

	private String getSize(File file) {
		double size1 = getDirSize(file) / 1024 / 1024;
		String size = String.valueOf(size1);
		return size + " MB";
	}

	private String getFormat(File dir) {
		File[] subFiles = dir.listFiles();
		for (File file : subFiles) {
			if (file.isFile()) {
				if (file.getName().contains(".tar")) {
					return "TAR";
				} else if (file.getName().contains(".dup")) {
					return "DUP";
				}
			}
		}
		return null;
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

		String Days = mContext.getText(R.string.days).toString();
		String Hours = mContext.getText(R.string.hours).toString();
		String Mins = mContext.getText(R.string.mins).toString();
		String ago = " " + mContext.getText(R.string.ago).toString();

		if (days <= 1) {
			Days = mContext.getText(R.string.day).toString();
		}

		if (hours <= 1) {
			Hours = mContext.getText(R.string.hour).toString();
		}

		if (mins <= 1) {
			Mins = mContext.getText(R.string.min).toString();
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

		/*
		 * 
		 * if (days < 7) { int flags = DateUtils.FORMAT_SHOW_DATE |
		 * DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_TIME;
		 * 
		 * ago = String.valueOf(DateUtils.getRelativeTimeSpanString(
		 * lastModified, System.currentTimeMillis(), 0, flags));
		 * 
		 * }
		 * 
		 * String summary = ago;
		 */

	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			final Preference preference) {

		CharSequence[] rord = { mContext.getText(R.string.restore),
				mContext.getText(R.string.delete),
				mContext.getText(R.string.rename) };

		AlertDialog.Builder choose = new AlertDialog.Builder(this);
		choose.setTitle(preference.getTitle());
		choose.setItems(rord, new OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				if (arg1 == 0) {

					View view = View.inflate(mContext, R.layout.dialog_restore,
							null);

					final CheckBox checkbox = (CheckBox) view
							.findViewById(R.id.checkBoxR);

					CharSequence[] items = { "Kernel", "System", "Data",
							"SD-Ext" };
					boolean[] states = { true, true, true, true };
					AlertDialog.Builder builder = new AlertDialog.Builder(
							mContext);
					builder.setTitle(mContext.getText(R.string.restore) + " - "
							+ preference.getTitle());
					builder.setView(view);
					builder.setMultiChoiceItems(items, states,
							new OnMultiChoiceClickListener() {
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
								}
							})
							.setPositiveButton(
									mContext.getText(R.string.restore),
									new OnClickListener() {

										@SuppressLint("SimpleDateFormat")
										public void onClick(
												final DialogInterface dialog,
												final int which) {

											SparseBooleanArray CheCked = ((AlertDialog) dialog)
													.getListView()
													.getCheckedItemPositions();

											String str1 = ", \"noboot\"";
											String str2 = ", \"nosystem\"";
											String str3 = ", \"nodata\"";
											String str4 = ", \"nosd-ext\"";
											if (CheCked.get(0)) {
												str1 = "";
											}
											if (CheCked.get(1)) {
												str2 = "";
											}
											if (CheCked.get(2)) {
												str3 = "";
											}
											if (CheCked.get(3)) {
												str4 = "";
											}
											String str5 = str1 + str2 + str3
													+ str4;

											String backup = "";
											String space = "";

											String storage = "/sdcard";
											if (preference.getKey().equals(
													"external")) {
												storage = "/emmc";
											}

											if (checkbox.isChecked()) {

												backup = "echo 'backup_rom(\""
														+ MadEnvironment.getDefaultBackupPath(mContext)
														+ "/clockworkmod/backup/"
														+ (new SimpleDateFormat(
																"yyyy-MM-dd.HH.mm.ss")
																.format(Calendar
																		.getInstance()
																		.getTime()))
														+ "/\");' >> '/cache/recovery/extendedcommand';";

												space = "echo 'ui_print(\" \");' >> '/cache/recovery/extendedcommand';";

											}

											String[] cmd = new String[9];
											cmd[0] = "rm -rf /cache/recovery;";
											cmd[1] = "mkdir /cache/recovery;";
											cmd[2] = "echo 'ui_print(\" \");' > /cache/recovery/extendedcommand;";
											cmd[3] = "echo 'ui_print(\""
													+ Command.appSign(mContext)
													+ "\");' >> /cache/recovery/extendedcommand;";
											cmd[4] = "echo 'ui_print(\" \");' >> /cache/recovery/extendedcommand;";
											cmd[5] = backup;
											cmd[6] = space;
											cmd[7] = "echo 'restore_rom(\""
													+ storage
													+ "/clockworkmod/backup/"
													+ preference.getTitle()
													+ "\""
													+ str5
													+ ");' >> '/cache/recovery/extendedcommand';";
											cmd[8] = "reboot recovery";

											Command.s(cmd);
											Command.reboot(mContext, "recovery");

										}
									})
							.setNegativeButton(
									mContext.getText(R.string.cancel), null)
							.show();
				}

				if (arg1 == 1) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							mContext);
					builder.setTitle(mContext.getText(R.string.delete) + " - "
							+ preference.getTitle());
					builder.setMessage(
							mContext.getText(R.string.delete_backup_confirm))
							.setPositiveButton(
									mContext.getText(R.string.delete),
									new OnClickListener() {

										public void onClick(
												final DialogInterface dialog,
												final int which) {

											String storage = Environment
													.getExternalStorageDirectory()
													.getPath();
											if (preference.getKey().equals(
													"external")) {
												storage = MadEnvironment.getExternalSDCardPath();
											}

											new Delete().execute(storage
													+ "/clockworkmod/backup/"
													+ preference.getTitle());

										}
									})
							.setNegativeButton(
									mContext.getText(R.string.cancel), null)
							.show();
				}

				if (arg1 == 2) {

					View view = View.inflate(mContext, R.layout.dialog_backup,
							null);

					final EditText edittext = (EditText) view
							.findViewById(R.id.backupedit);
					edittext.setText(preference.getTitle());
					edittext.selectAll();

					AlertDialog.Builder builder = new AlertDialog.Builder(
							mContext);
					builder.setTitle(mContext.getText(R.string.rename));
					builder.setView(view);
					builder.setPositiveButton(
							mContext.getText(R.string.rename),
							new OnClickListener() {

								public void onClick(
										final DialogInterface dialog,
										final int which) {

									String storage = Environment
											.getExternalStorageDirectory()
											.getPath();
									if (preference.getKey().equals("external")) {
										storage = MadEnvironment.getExternalSDCardPath();
									}

									new Rename().execute(storage
											+ "/clockworkmod/backup/"
											+ preference.getTitle(), edittext
											.getText().toString().trim());

								}
							})
							.setNegativeButton(
									mContext.getText(R.string.cancel), null)
							.show();
				}
			}

		});
		choose.setNegativeButton(mContext.getText(R.string.cancel), null)
				.show();

		return false;
	}

	public class Rename extends AsyncTask<String, String, String> {

		private String getBackupName(String str) {

			if (str.length() > 0) {

				StringBuilder builder = new StringBuilder();

				String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.-_";

				char[] allowedList = allowed.toCharArray();
				int allowedLength = allowedList.length;

				boolean lastWasCharacter = false;

				for (char c : str.toCharArray()) {
					int i = 0;
					for (char a : allowedList) {
						i += 1;
						if (a == c) {
							builder.append(c);
							lastWasCharacter = true;
							break;
						} else if (i == allowedLength && lastWasCharacter) {
							builder.append("_");
							lastWasCharacter = false;
						}
					}
				}

				String backupname = builder.toString();

				if (backupname.length() > 0) {
					return backupname;
				}
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected String doInBackground(String... arg) {

			String backupName = getBackupName(arg[1]);

			if (!MadUtils.isEmpty(backupName)) {
				File dir = new File(arg[0]);

				File newPath = new File(dir.getParent() + "/" + backupName);
				if (newPath.exists()) {
					return mContext.getText(R.string.dir_exists).toString();
				} else {
					dir.renameTo(newPath);
				}
			} else {
				return mContext.getText(R.string.invalidname).toString();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String r) {

			setSupportProgressBarIndeterminateVisibility(false);
			if (r != null) {
				Toast.makeText(mContext, r, Toast.LENGTH_SHORT).show();
			} else {
				loadBackups();
			}
		}
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

			return null;
		}

		@Override
		protected void onPostExecute(String r) {
			setSupportProgressBarIndeterminateVisibility(false);
			loadBackups();
		}
	}

}