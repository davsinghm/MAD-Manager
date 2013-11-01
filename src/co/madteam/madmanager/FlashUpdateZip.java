package co.madteam.madmanager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.util.SparseBooleanArray;
import android.widget.Toast;
import co.madteam.madmanager.utilities.MadEnvironment;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.analytics.tracking.android.EasyTracker;

public class FlashUpdateZip extends SherlockPreferenceActivity {

	PreferenceCategory PrefCat;
	Context mContext;

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
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(getText(R.string.add_files))
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						Intent i = new Intent(getApplicationContext(),
								SelectFilePref.class);
						i.putExtra("select", "update");
						startActivityForResult(i, 0);
						return false;
					}

				})
				.setIcon(
						Theme.isActionBarLight(getApplicationContext()) ? R.drawable.action_content_new_light
								: R.drawable.action_content_new)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add(getText(R.string.flash_updates))
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						if (PrefCat.getPreferenceCount() == 0) {
							Toast.makeText(getApplicationContext(),
									getText(R.string.add_atleast_one_package),
									Toast.LENGTH_SHORT).show();
							return false;
						}

						CharSequence[] items = { getText(R.string.backup_current_rom),
								getText(R.string.wipe_data), getText(R.string.wipe_dalvik_cache) };
						boolean[] states = { false, false, true };
						AlertDialog.Builder builder = new AlertDialog.Builder(
								mContext);
						builder.setTitle(getText(R.string.flash));
						builder.setMultiChoiceItems(items, states,
								new OnMultiChoiceClickListener() {
									public void onClick(DialogInterface dialog,
											int which, boolean isChecked) {
									}
								});
						builder.setPositiveButton(getText(R.string.ok),
								new OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {

										SparseBooleanArray CheCked = ((AlertDialog) dialog)
												.getListView()
												.getCheckedItemPositions();
										boolean backupCurrentROM = CheCked
												.get(0);
										boolean wipeData = CheCked.get(1);
										boolean wipeDalvikCache = CheCked
												.get(2);

										String[] cmd = new String[4];
										cmd[0] = "rm -rf /cache/recovery;";
										cmd[1] = "mkdir /cache/recovery;";
										cmd[2] = "echo 'ui_print(\" \");' > /cache/recovery/extendedcommand;";
										cmd[3] = "echo 'ui_print(\""
												+ Command.appSign(getApplicationContext())
												+ "\");' >> /cache/recovery/extendedcommand;";

										Command.s(cmd);

										if (backupCurrentROM) {

											String[] backup = new String[2];
											backup[0] = "echo 'ui_print(\" \");' >> /cache/recovery/extendedcommand;";
											backup[1] = "echo 'backup_rom(\""
													+ MadEnvironment.getDefaultBackupPath(getApplicationContext())
													+ "/clockworkmod/backup/"
													+ (new SimpleDateFormat(
															"yyyy-MM-dd.HH.mm.ss")
															.format(Calendar
																	.getInstance()
																	.getTime()))
													+ "/\");' >> '/cache/recovery/extendedcommand';";

											Command.s(backup);
										}

										if (wipeData) {

											String[] wipedata = new String[5];
											wipedata[0] = "echo 'ui_print(\" \");' >> /cache/recovery/extendedcommand;";
											wipedata[1] = "echo 'ui_print(\"-- Wiping data...\");' >> '/cache/recovery/extendedcommand';";
											wipedata[2] = "echo 'format(\"/data\");' >> '/cache/recovery/extendedcommand';";
											wipedata[3] = "echo 'format(\"/cache\");' >> '/cache/recovery/extendedcommand';";
											wipedata[4] = "echo 'format(\"/sd-ext\");' >> '/cache/recovery/extendedcommand';";
											Command.s(wipedata);
											
										} else if (wipeDalvikCache) {

											String[] wipedalvik = new String[8];
											wipedalvik[0] = "echo 'ui_print(\" \");' >> /cache/recovery/extendedcommand;";
											wipedalvik[1] = "echo 'ui_print(\"Wiping dalvik cache...\");' >> '/cache/recovery/extendedcommand';";
											wipedalvik[2] = "echo 'mount(\"/cache\");' >> '/cache/recovery/extendedcommand';";
											wipedalvik[3] = "echo 'mount(\"/data\");' >> '/cache/recovery/extendedcommand';";
											wipedalvik[4] = "echo 'mount(\"/sd-ext\");' >> '/cache/recovery/extendedcommand';";
											wipedalvik[5] = "echo 'run_program(\"/sbin/busybox\", \"rm\", \"-rf\", \"/cache/dalvik-cache\");' >> '/cache/recovery/extendedcommand';";
											wipedalvik[6] = "echo 'run_program(\"/sbin/busybox\", \"rm\", \"-rf\", \"/data/dalvik-cache\");' >> '/cache/recovery/extendedcommand';";
											wipedalvik[7] = "echo 'run_program(\"/sbin/busybox\", \"rm\", \"-rf\", \"/sd-ext/dalvik-cache\");' >> '/cache/recovery/extendedcommand';";
											Command.s(wipedalvik);

										}

										for (int i = 0; i < PrefCat.getPreferenceCount(); i++) {
											String location = PrefCat.getPreference(i).getSummary().toString();
											if (MadEnvironment.getExternalSDCardPath() != null) {
												location = location.replace(MadEnvironment.getExternalSDCardPath(), "/emmc");
											}
											location = location.replace(Environment.getExternalStorageDirectory().getPath(), "/sdcard");
											location.replace("/mnt", "");
											String zip = "echo 'install_zip(\""
													+ location
													+ "\");' >> '/cache/recovery/extendedcommand';";
											Command.line(zip);

										}

										Command.line("reboot recovery");
										Command.reboot(mContext, "recovery");


									}
								});
						builder.setNegativeButton(getApplicationContext()
								.getText(R.string.cancel), null);
						builder.show();
						return false;
					}

				}).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add(getText(R.string.remove_all))
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						if (PrefCat != null) {
							PrefCat.removeAll();
						}
						return false;
					}

				})
				.setIcon(
						Theme.isActionBarLight(getApplicationContext()) ? R.drawable.action_delete_light
								: R.drawable.action_delete)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		setTheme(Theme.getTheme(this));

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);

		setTitle(getText(R.string.flash_update));
		
		setContentView(R.layout.pref_screen);

		EasyTracker.getInstance().activityStart(this);
		  
		mContext = this;
		
		Intent intent = getIntent();
		
		Bundle bundle = intent.getExtras();

		PreferenceScreen PrefScreen = getPreferenceManager()
				.createPreferenceScreen(this);

		PrefCat = new PreferenceCategory(this);
		PrefCat.setTitle(getText(R.string.PACKAGES_TO_FLASH));
		PrefScreen.addPreference(PrefCat);

		if (bundle != null) {
			for (int i = 0; i < bundle.size(); i++) {

				String str = bundle.getString(i + "");
				Preference pref = new Preference(this);
				pref.setTitle(str.substring(str.lastIndexOf("/") + 1));
				pref.setSummary(str);
				pref.setEnabled(true);
				pref.setLayoutResource(R.layout.pref_small);
				PrefCat.addPreference(pref);
			}
		}
		
		if ("android.intent.action.VIEW".equals(intent.getAction())) {
			Uri file = intent.getData();
			String str = file.getPath();

			if (str != null) {

				Preference pref = new Preference(this);
				pref.setTitle(str.substring(str.lastIndexOf("/") + 1));
				pref.setSummary(str);
				pref.setEnabled(true);
				pref.setLayoutResource(R.layout.pref_small);
				PrefCat.addPreference(pref);

			}
			
		}

		setPreferenceScreen(PrefScreen);

	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			final Preference preference) {

		CharSequence[] items = { getText(R.string.remove) };

		AlertDialog.Builder choose = new AlertDialog.Builder(this);
		choose.setTitle(preference.getTitle());
		choose.setItems(items, new OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {

				PrefCat.removePreference(preference);
			}
		});
		choose.setNegativeButton(getText(R.string.cancel), null);
		choose.show();

		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (data.hasExtra("update")) {

				if (PrefCat != null) {

					String str = data.getExtras().getString("update");
					Preference pref = new Preference(this);
					pref.setTitle(str.substring(str.lastIndexOf("/") + 1));
					pref.setSummary(str);
					pref.setEnabled(true);
					pref.setLayoutResource(R.layout.pref_small);
					PrefCat.addPreference(pref);

				}

			}
		}
	}
	
	  @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance().activityStop(this);
	  }
}
