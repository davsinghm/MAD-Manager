package co.madteam.madmanager.dm;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import co.madteam.madmanager.FlashUpdateZip;
import co.madteam.madmanager.PrefDownloads;
import co.madteam.madmanager.R;
import co.madteam.madmanager.Theme;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.Window;
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
import android.support.v4.app.NavUtils;
import android.text.format.DateUtils;
import android.widget.ListView;
import android.widget.TextView;

public class DownloadManager extends SherlockPreferenceActivity {

	private Context mContext;
	private PreferenceScreen PrefScreen;
	private File directory;
	private boolean isActionModeEnabled;
	private ActionMode actionMode;
	private int mComparator;

	public static String downloadDirectory(Context context) {

		String defaultDirectory = Environment.getExternalStorageDirectory()
				.getPath() + "/madmanager";
		String dir = PreferenceManager.getDefaultSharedPreferences(context)
				.getString("download_location", defaultDirectory);
		return dir;
	}

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

		setContentView(R.layout.pref_screen_restore);

		setSupportProgressBarIndeterminateVisibility(false);

		mContext = this;

		directory = new File(downloadDirectory(this));

		isActionModeEnabled = false;

		loadDownloads();
	}

	private String getSize(long l) {

		DecimalFormat decimalFormat = new DecimalFormat("#.##");

		double length = l;
		double size = length / 1024 / 1024;
		return decimalFormat.format(size) + " MB";
	}

	private String getLastModified(long l) {

		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
				| DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_CAP_AMPM;

		String ago = DateUtils.formatDateTime(mContext, l, flags).toString();

		return ago;

	}

	public void loadDownloads() {

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

		ArrayList<Bundle> arrayList = new ArrayList<Bundle>();

		if (!directory.exists()) {
			directory.mkdirs();
		}

		if (directory.canRead()) {

			File[] files = directory.listFiles();
			for (File file : files) {

				if (file.isFile() && file.getName().endsWith(".zip")) {

					Bundle bundle = new Bundle();
					bundle.putString("filename", file.getName());
					bundle.putString("dir", file.getParent());
					bundle.putLong("length", file.length());
					bundle.putLong("timestamp", file.lastModified());
					arrayList.add(bundle);
				}
			}
		}

		if (arrayList.isEmpty()) {
			ListView list = (ListView) findViewById(android.R.id.list);
			list.setVisibility(ListView.GONE);
			TextView tv = (TextView) findViewById(R.id.nobackups);
			tv.setText("No downloads");
			tv.setVisibility(TextView.VISIBLE);
			
		} else {
			Comparator<? super Bundle> filecomparator = new Comparator<Bundle>() {
				public int compare(Bundle file1, Bundle file2) {
					return String.valueOf(file1.getString("filename"))
							.compareTo(file2.getString("filename"));
				}
			};

			if (mComparator == 1) {
				filecomparator = new Comparator<Bundle>() {
					public int compare(Bundle file1, Bundle file2) {
						return Long.valueOf(file2.getLong("length")).compareTo(
								file1.getLong("length"));
					}
				};
			} else if (mComparator == 2) {
				filecomparator = new Comparator<Bundle>() {
					public int compare(Bundle file1, Bundle file2) {
						return Long.valueOf(file1.getLong("timestamp"))
								.compareTo(file2.getLong("timestamp"));
					}
				};
			}
			Bundle[] downloadfiles = new Bundle[arrayList.size()];

			arrayList.toArray(downloadfiles);

			Arrays.sort(downloadfiles, filecomparator);

			for (Bundle bundle : downloadfiles) {

				PrefDownloads pref = new PrefDownloads(this);
				pref.setTitle(bundle.getString("filename"));
				pref.setEnabled(true);
				pref.setSummary(bundle.getString("dir"));
				pref.setDate(getLastModified(bundle.getLong("timestamp")));
				pref.setInfo(getSize(bundle.getLong("length")));
				pref.setSupportIcon(R.drawable.ic_prf_zip);
				pref.setSelected(false);
				PrefScreen.addPreference(pref);
			}
		}

		setPreferenceScreen(PrefScreen);

	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			final Preference p) {

		if (!isActionModeEnabled) {
			actionMode = startActionMode(new AnActionModeOfEpicProportions());
		}

		int checked = 0;
		for (int i = 0; i < PrefScreen.getPreferenceCount(); i++) {

			PrefDownloads preference = (PrefDownloads) PrefScreen
					.getPreference(i);

			if (preference.isSelected()) {
				checked += 1;
			}

		}

		if (checked > 0) {
			actionMode.setTitle("Selected " + checked + " out of "
					+ PrefScreen.getPreferenceCount());

		} else if (checked == 0) {
			if (actionMode != null) {
				actionMode.finish();
			}
		}

		return false;
	}

	private final class AnActionModeOfEpicProportions implements
			ActionMode.Callback {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {

			MenuItem flash = menu.add(getText(R.string.flash));
			flash.setIcon(Theme.isActionBarLight(getApplicationContext()) ? R.drawable.action_flash_light
					: R.drawable.action_flash);
			flash.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			flash.setOnMenuItemClickListener(new OnMenuItemClickListener() {

				@Override
				public boolean onMenuItemClick(MenuItem item) {

					Bundle bundle = new Bundle();

					int id = 0;
					for (int i = 0; i < PrefScreen.getPreferenceCount(); i++) {

						PrefDownloads preference = (PrefDownloads) PrefScreen
								.getPreference(i);

						if (preference.isSelected()) {
							String location = preference.getSummary() + "/" + preference.getTitle();
							bundle.putString(id + "", location);
							id += 1;
						}

					}
					
					if (actionMode != null) {
						actionMode.finish();
					}
					
					if (!bundle.isEmpty()) {

						Intent intent = new Intent(mContext, FlashUpdateZip.class);
						intent.putExtras(bundle);
						startActivity(intent);

					}

					return false;
				}

			});

			menu.add(getText(R.string.delete))
					.setIcon(
							Theme.isActionBarLight(getApplicationContext()) ? R.drawable.action_delete_light
									: R.drawable.action_delete)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			menu.add(getText(R.string.select_all))
					.setIcon(
							Theme.isActionBarLight(getApplicationContext()) ? R.drawable.action_select_all_light
									: R.drawable.action_select_all)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			
			MenuItem sort = menu.add(getText(R.string.sort));
			sort.setIcon(Theme.isActionBarLight(getApplicationContext()) ? R.drawable.action_sort_light
									: R.drawable.action_sort);
					sort.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				sort.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							
							CharSequence[] rord = { getText(R.string.name), getText(R.string.size), getText(R.string.date) };

							AlertDialog.Builder choose = new AlertDialog.Builder(mContext);
							choose.setTitle(getText(R.string.sort_by));
							choose.setItems(rord, new OnClickListener() {

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
									loadDownloads();
									if (actionMode != null) {
										actionMode.finish();
									}
								}
							}).setPositiveButton(getText(R.string.cancel), null);
							choose.show();
							
							return false;
						}});

			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			isActionModeEnabled = true;
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			if (item.getTitle().equals(getText(R.string.delete))) {

				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(mContext.getText(R.string.delete));
				builder.setMessage(getText(R.string.confirm_delete_sel_files))
						.setPositiveButton(mContext.getText(R.string.delete),
								new OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {

										ArrayList<String> arraylist = new ArrayList<String>();
										for (int i = 0; i < PrefScreen
												.getPreferenceCount(); i++) {

											PrefDownloads preference = (PrefDownloads) PrefScreen
													.getPreference(i);

											if (preference.isSelected()) {

												String location = preference
														.getSummary()
														.toString();
												arraylist.add(location);
											}

										}
										if (!arraylist.isEmpty()) {

											String[] list = new String[arraylist
													.size()];

											new Delete().execute(arraylist
													.toArray(list));

										}

									}
								})
						.setNegativeButton(mContext.getText(R.string.cancel),
								null).show();

			} else if (item.getTitle().equals(getText(R.string.select_all))) {

				for (int i = 0; i < PrefScreen.getPreferenceCount(); i++) {

					PrefDownloads preference = (PrefDownloads) PrefScreen
							.getPreference(i);
					preference.setSelected(true);

				}

				actionMode.setTitle(getText(R.string.selected) + " "
						+ PrefScreen.getPreferenceCount() + " " + getText(R.string.out_of) + " "
						+ PrefScreen.getPreferenceCount());

			}
			return false;

		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			isActionModeEnabled = false;

			for (int i = 0; i < PrefScreen.getPreferenceCount(); i++) {

				PrefDownloads preference = (PrefDownloads) PrefScreen
						.getPreference(i);

				preference.setSelected(false);
			}
		}
	}

	public class Delete extends AsyncTask<String[], String, String> {

		@Override
		protected void onPreExecute() {
			if (actionMode != null) {
				actionMode.finish();
			}
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected String doInBackground(String[]... arg) {

			for (int i = 0; i < arg[0].length; i++) {
				File file = new File(arg[0][i]);
				file.delete();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {

			setSupportProgressBarIndeterminateVisibility(false);
			loadDownloads();

		}
	}

}