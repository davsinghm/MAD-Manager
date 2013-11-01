package co.madteam.madmanager;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.analytics.tracking.android.EasyTracker;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.format.DateUtils;
import android.widget.ListView;
import android.widget.TextView;

public class SelectFilePref extends SherlockPreferenceActivity {

	PreferenceScreen PrefScreen;
	private File mDirectory;
	private boolean mBaseband;
	private boolean mUpdate;
	private int mComparator;
	public static Context mContext;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuItem sort = menu.add(getText(R.string.sort));
		sort.setIcon(Theme.isActionBarLight(getApplicationContext()) ? R.drawable.action_sort_light
				: R.drawable.action_sort);
		sort.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		sort.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {

				CharSequence[] rord = { getText(R.string.name), getText(R.string.size), getText(R.string.date), getText(R.string.type) };

				AlertDialog.Builder choose = new AlertDialog.Builder(mContext);
				choose.setTitle(getText(R.string.sort_by));
				choose.setSingleChoiceItems(rord, mComparator,
						new OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {
								if (arg1 == 0) {
									mComparator = 0;
								}
								if (arg1 == 1) {
									mComparator = 1;
								}
								if (arg1 == 2) {
									mComparator = 2;
								}
								if (arg1 == 3) {
									mComparator = 3;
								}
								PreferenceManager
										.getDefaultSharedPreferences(
												getApplicationContext()).edit()
										.putInt("sort", mComparator).commit();
								loadFiles(mDirectory);
								arg0.dismiss();

							}
						}).setPositiveButton(getText(R.string.cancel), null);
				choose.show();

				return false;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	public String getSize(File file) {

		String summary = "";

		if (file.isFile()) {
			double size1 = file.length();
			double size = (size1 / 1024 / 1024);

			DecimalFormat decimalFormat = new DecimalFormat("#.##");

			summary = decimalFormat.format(size) + " MB";
		}
		return summary;
	}

	public String noOfFolders(File dir) {

		int files = 0;
		int folders = 0;

		String folder = " " + getText(R.string.folders) + ", ";
		String file = " " + getText(R.string.files);
		File[] Afiles = dir.listFiles();

		if (Afiles != null) {
			for (File dir2 : Afiles) {

				if (dir2.isFile()) {
					files += 1;
				} else if (dir2.isDirectory()) {
					folders += 1;
				}
			}
		}

		if (folders <= 1) {
			folder = " " + getText(R.string.folder) + ", ";
		}
		if (files <= 1) {
			file = " " + getText(R.string.file);
		}

		String summary = folders + folder + files + file;

		return summary;

	}

	private String getLastModified(long l) {

		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
				| DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_CAP_AMPM;

		String ago = DateUtils.formatDateTime(mContext, l, flags).toString();

		return ago;

	}

	public void loadFiles(final File dir) {

		new AsyncTask<Void, Void, String>() {

			@Override
			protected void onPreExecute() {

				setSupportProgressBarIndeterminateVisibility(true);

				ListView list = (ListView) findViewById(android.R.id.list);
				list.setVisibility(ListView.VISIBLE);
				TextView tv = (TextView) findViewById(R.id.nobackups);
				tv.setVisibility(TextView.GONE);

				getSupportActionBar().setSubtitle(dir.getAbsolutePath());

				if (PrefScreen != null) {
					PrefScreen.removeAll();
					PrefScreen = getPreferenceManager().createPreferenceScreen(
							mContext);
				} else {
					PrefScreen = getPreferenceManager().createPreferenceScreen(
							mContext);
				}
			}

			@Override
			protected String doInBackground(Void... params) {

				mDirectory = dir;
				
				if (!Environment.MEDIA_MOUNTED.equals(Environment
							.getExternalStorageState())) {
					return getText(R.string.sdcard_not_mounted).toString();
				}

				if (mDirectory.canRead()) {
					File[] files = mDirectory.listFiles();

					switch (mComparator) {
					case 0:
						Arrays.sort(files, getComparator(0));

						for (File file : files) {

							if (file.isDirectory()
									&& !file.getName().startsWith(".")) {

								PrefFile Directory = new PrefFile(mContext);
								Directory.setTitle(file.getName());
								Directory.setSummary(noOfFolders(file
										.getAbsoluteFile()));
								Directory.setEnabled(true);
								Directory.setInfo(getLastModified(file
										.lastModified()));
								Directory
										.setSupportIcon(R.drawable.ic_prf_folder);
								PrefScreen.addPreference(Directory);
							}

							if (mBaseband) {
								if (file.isFile()
										&& !file.getName().contains(".")
										&& file.getName().toLowerCase()
												.contains("amss")) {
									PrefFile File = new PrefFile(mContext);
									File.setTitle(file.getName());
									File.setSummary(getSize(file));
									File.setEnabled(true);
									File.setInfo(getLastModified(file
											.lastModified()));
									File.setSupportIcon(R.drawable.ic_prf_baseband);

									PrefScreen.addPreference(File);

								}

							} else if (mUpdate) {

								if (file.isFile()
										&& file.getName().toLowerCase()
												.endsWith(".zip")) {
									PrefFile File = new PrefFile(mContext);
									File.setTitle(file.getName());
									File.setSummary(getSize(file));
									File.setEnabled(true);
									File.setInfo(getLastModified(file
											.lastModified()));
									File.setSupportIcon(R.drawable.ic_prf_zip);

									PrefScreen.addPreference(File);
								}
							}
						}
						break;

					case 1:
						Arrays.sort(files, getComparator(0));

						for (File file : files) {

							if (file.isDirectory()
									&& !file.getName().startsWith(".")) {

								PrefFile Directory = new PrefFile(mContext);
								Directory.setTitle(file.getName());
								Directory.setSummary(noOfFolders(file
										.getAbsoluteFile()));
								Directory.setEnabled(true);
								Directory.setInfo(getLastModified(file
										.lastModified()));
								Directory
										.setSupportIcon(R.drawable.ic_prf_folder);
								PrefScreen.addPreference(Directory);
							}
						}
						if (mBaseband) {
							Arrays.sort(files, getComparator(1));

							for (File file : files) {
								if (file.isFile()
										&& !file.getName().contains(".")
										&& file.getName().toLowerCase()
												.contains("amss")) {
									PrefFile File = new PrefFile(mContext);
									File.setTitle(file.getName());
									File.setSummary(getSize(file));
									File.setEnabled(true);
									File.setInfo(getLastModified(file
											.lastModified()));
									File.setSupportIcon(R.drawable.ic_prf_baseband);

									PrefScreen.addPreference(File);
								}
							}

						} else if (mUpdate) {
							Arrays.sort(files, getComparator(1));

							for (File file : files) {

								if (file.isFile()
										&& file.getName().toLowerCase()
												.endsWith(".zip")) {
									PrefFile File = new PrefFile(mContext);
									File.setTitle(file.getName());
									File.setSummary(getSize(file));
									File.setEnabled(true);
									File.setInfo(getLastModified(file
											.lastModified()));
									File.setSupportIcon(R.drawable.ic_prf_zip);

									PrefScreen.addPreference(File);
								}
							}
						}
						break;

					case 2:
						Arrays.sort(files, getComparator(2));

						for (File file : files) {

							if (file.isDirectory()
									&& !file.getName().startsWith(".")) {

								PrefFile Directory = new PrefFile(mContext);
								Directory.setTitle(file.getName());
								Directory.setSummary(noOfFolders(file
										.getAbsoluteFile()));
								Directory.setEnabled(true);
								Directory.setInfo(getLastModified(file
										.lastModified()));
								Directory
										.setSupportIcon(R.drawable.ic_prf_folder);
								PrefScreen.addPreference(Directory);
							}

							if (mBaseband) {
								if (file.isFile()
										&& !file.getName().contains(".")
										&& file.getName().toLowerCase()
												.contains("amss")) {
									PrefFile File = new PrefFile(mContext);
									File.setTitle(file.getName());
									File.setSummary(getSize(file));
									File.setEnabled(true);
									File.setInfo(getLastModified(file
											.lastModified()));
									File.setSupportIcon(R.drawable.ic_prf_baseband);

									PrefScreen.addPreference(File);

								}

							} else if (mUpdate) {

								if (file.isFile()
										&& file.getName().toLowerCase()
												.endsWith(".zip")) {
									PrefFile File = new PrefFile(mContext);
									File.setTitle(file.getName());
									File.setSummary(getSize(file));
									File.setEnabled(true);
									File.setInfo(getLastModified(file
											.lastModified()));
									File.setSupportIcon(R.drawable.ic_prf_zip);

									PrefScreen.addPreference(File);
								}
							}
						}
						break;

					case 3:
						Arrays.sort(files, getComparator(0));

						for (File file : files) {

							if (file.isDirectory()
									&& !file.getName().startsWith(".")) {

								PrefFile Directory = new PrefFile(mContext);
								Directory.setTitle(file.getName());
								Directory.setSummary(noOfFolders(file
										.getAbsoluteFile()));
								Directory.setEnabled(true);
								Directory.setInfo(getLastModified(file
										.lastModified()));
								Directory
										.setSupportIcon(R.drawable.ic_prf_folder);
								PrefScreen.addPreference(Directory);
							}
						}
						if (mBaseband) {
							for (File file : files) {
								if (file.isFile()
										&& !file.getName().contains(".")
										&& file.getName().toLowerCase()
												.contains("amss")) {
									PrefFile File = new PrefFile(mContext);
									File.setTitle(file.getName());
									File.setSummary(getSize(file));
									File.setEnabled(true);
									File.setInfo(getLastModified(file
											.lastModified()));
									File.setSupportIcon(R.drawable.ic_prf_baseband);

									PrefScreen.addPreference(File);
								}
							}

						} else if (mUpdate) {
							for (File file : files) {

								if (file.isFile()
										&& file.getName().toLowerCase()
												.endsWith(".zip")) {
									PrefFile File = new PrefFile(mContext);
									File.setTitle(file.getName());
									File.setSummary(getSize(file));
									File.setEnabled(true);
									File.setInfo(getLastModified(file
											.lastModified()));
									File.setSupportIcon(R.drawable.ic_prf_zip);

									PrefScreen.addPreference(File);
								}
							}
						}
						break;
					}
				}
				
				if (PrefScreen.getPreferenceCount() == 0) {
					return getText(R.string.no_files).toString();
				}

				return null;
			}

			@Override
			protected void onPostExecute(String string) {
				setPreferenceScreen(PrefScreen);
				setSupportProgressBarIndeterminateVisibility(false);

				if (string != null) {
					ListView list = (ListView) findViewById(android.R.id.list);
					list.setVisibility(ListView.GONE);
					TextView tv = (TextView) findViewById(R.id.nobackups);
					tv.setText(string);
					tv.setVisibility(TextView.VISIBLE);
				}
			}

		}.execute();

	}

	protected Comparator<? super File> getComparator(int type) {

		Comparator<? super File> filecomparator = null;

		switch (type) {
		case 0:
			filecomparator = new Comparator<File>() {
				public int compare(File file1, File file2) {
					return String.valueOf(file1.getName()).compareTo(
							file2.getName());
				}
			};
			break;
		case 1:
			filecomparator = new Comparator<File>() {
				public int compare(File file1, File file2) {
					return Long.valueOf(file1.length()).compareTo(
							file2.length());
				}
			};
			break;
		case 2:
			filecomparator = new Comparator<File>() {
				public int compare(File file1, File file2) {
					return Long.valueOf(file2.lastModified()).compareTo(
							file1.lastModified());
				}
			};
			break;
		}
		if (filecomparator != null) {
			return filecomparator;
		}

		return new Comparator<File>() {
			public int compare(File file1, File file2) {
				return String.valueOf(file1.getName()).compareTo(
						file2.getName());
			}
		};

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			if (!mDirectory.getAbsolutePath().equals("/")) {
				loadFiles(mDirectory.getParentFile());
			} else {
				finish();
			}

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		if (mDirectory.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getPath()) || mDirectory.getAbsolutePath().equals("/sdcard")
				|| mDirectory.getAbsolutePath().equals("/mnt/sdcard")
				|| mDirectory.getAbsolutePath().equals("/")) {
			super.onBackPressed();
		} else {
			loadFiles(mDirectory.getParentFile());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setTheme(Theme.getTheme(this));

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);

		mComparator = PreferenceManager.getDefaultSharedPreferences(this)
				.getInt("sort", 0);

		setContentView(R.layout.pref_screen_restore);

		EasyTracker.getInstance().activityStart(this);

		mContext = this;

		mDirectory = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/");

		mUpdate = false;

		mBaseband = false;

		String getExtra = getIntent().getExtras().getString("select");

		if (getExtra.equals("update")) {
			mUpdate = true;
		} else if (getExtra.equals("baseband")) {
			mBaseband = true;
			setTitle(getText(R.string.flash_baseband));
		}

		loadFiles(mDirectory);

	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		final File selected = new File(mDirectory + "/" + preference.getTitle());

		if (selected.isDirectory()) {
			loadFiles(selected);
		}

		if (selected.isFile()) {
			if (mBaseband) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getText(R.string.flash) + " - "
						+ selected.getName());
				builder.setMessage(getText(R.string.confirm_flash_baseband))
						.setPositiveButton(getText(R.string.flash),
								new OnClickListener() {

									public void onClick(
											final DialogInterface dialog,
											final int which) {

										new FlashBaseband().execute(selected
												.getAbsolutePath());

									}
								})
						.setNegativeButton(getText(R.string.cancel), null)
						.show();

			}
			if (mUpdate) {
				Intent data = new Intent();
				data.putExtra("update", selected.getAbsolutePath());
				setResult(RESULT_OK, data);
				finish();
			}

		}

		return false;
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
}
