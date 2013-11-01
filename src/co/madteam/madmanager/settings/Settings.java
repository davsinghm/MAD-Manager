package co.madteam.madmanager.settings;

import co.madteam.madmanager.R;
import co.madteam.madmanager.Theme;
import co.madteam.madmanager.utilities.MadEnvironment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;

public class Settings extends SherlockPreferenceActivity {

	private SharedPreferences.Editor mEditor;
	private SharedPreferences mSharedPreferences;
	private Context mContext;

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

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.pref_screen);

		EasyTracker.getInstance().activityStart(this);

		mContext = this;

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		mEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();

		PreferenceScreen PrefScreen = getPreferenceManager()
				.createPreferenceScreen(this);

		PreferenceCategory General = new PreferenceCategory(this);
		General.setTitle(getText(R.string.GENERAL));
		PrefScreen.addPreference(General);

		CheckBoxPreference UpdateNoti = new CheckBoxPreference(this);
		UpdateNoti.setTitle(getText(R.string.settings_update_noti));
		UpdateNoti.setSummary(getText(R.string.settings_update_noti_sum));
		UpdateNoti.setChecked(mSharedPreferences.getBoolean(
				"check_for_updates", true));
		UpdateNoti.setEnabled(true);
		UpdateNoti.setLayoutResource(R.layout.pref);
		UpdateNoti.setWidgetLayoutResource(R.layout.pref_widget_checkbox);
		UpdateNoti.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference arg0) {
						mEditor.putBoolean("check_for_updates",
								((CheckBoxPreference) arg0).isChecked());
						return false;
					}
				});
		General.addPreference(UpdateNoti);

		Preference Frequency = new Preference(this);
		Frequency.setTitle(getText(R.string.settings_update_check_freq));
		Frequency.setSummary(getText(R.string.settings_update_check_freq_des));
		Frequency.setEnabled(true);
		Frequency.setLayoutResource(R.layout.pref);
		Frequency.setOnPreferenceClickListener(new UpdateCheckFrequency(mContext,
				mSharedPreferences.getInt(
						"update_check_freq", 4), mEditor));
		General.addPreference(Frequency);

		CheckBoxPreference UseExternalStorage = new CheckBoxPreference(this);
		UseExternalStorage.setTitle(getText(R.string.settings_ext_storage));
		UseExternalStorage
				.setSummary(getText(R.string.settings_ext_storage_sum));
		if (MadEnvironment.isExternalSDCardMounted()) {
			UseExternalStorage.setChecked(mSharedPreferences.getBoolean(
					"use_external_storage", true));
			UseExternalStorage.setEnabled(true);
		} else {
			UseExternalStorage.setChecked(false);
			UseExternalStorage.setEnabled(false);
		}
		UseExternalStorage.setLayoutResource(R.layout.pref);
		UseExternalStorage.setWidgetLayoutResource(R.layout.pref_widget_checkbox);
		UseExternalStorage.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						mEditor.putBoolean("use_external_storage",
								((CheckBoxPreference) arg0).isChecked());
						return false;
					}
				});
		General.addPreference(UseExternalStorage);

		PreferenceCategory Appearance = new PreferenceCategory(this);
		Appearance.setTitle(getText(R.string.APPEARANCE));
		PrefScreen.addPreference(Appearance);

		Preference Theme = new Preference(this);
		Theme.setTitle(getText(R.string.settings_theme));
		Theme.setSummary(getText(R.string.settings_theme_des));
		Theme.setEnabled(true);
		Theme.setLayoutResource(R.layout.pref);
		Theme.setOnPreferenceClickListener(new SelectTheme(mContext,
				mSharedPreferences.getInt("theme", 1), mEditor));
		Appearance.addPreference(Theme);

		CheckBoxPreference ICSNotification = new CheckBoxPreference(this);
		ICSNotification.setTitle(getText(R.string.settings_ics_notification));
		ICSNotification.setSummary(getText(R.string.settings_ics_notification_des));
		ICSNotification.setChecked(!mSharedPreferences.getBoolean(
				"use_default_noti", false));
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD || Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD_MR1) {
			ICSNotification.setEnabled(true);
		} else {
			ICSNotification.setEnabled(false);
			ICSNotification.setChecked(false);
		}
		ICSNotification.setLayoutResource(R.layout.pref);
		ICSNotification.setWidgetLayoutResource(R.layout.pref_widget_checkbox);
		ICSNotification.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference arg0) {
						mEditor.putBoolean("use_default_noti",
								!((CheckBoxPreference) arg0).isChecked());
						return false;
					}
				});
		Appearance.addPreference(ICSNotification);

		PreferenceCategory Download = new PreferenceCategory(this);
		Download.setTitle(getText(R.string.DOWNLOAD));
		PrefScreen.addPreference(Download);

		String downloadDir = Environment.getExternalStorageDirectory()
				+ "/madmanager";

		EditTextPreference DownloadDirectory = new EditTextPreference(this);
		DownloadDirectory.setTitle(getText(R.string.settings_dl_dir));
		DownloadDirectory.setSummary(mSharedPreferences.getString(
				"download_location", downloadDir));
		DownloadDirectory.setDialogTitle(getText(R.string.settings_dl_dir));
		DownloadDirectory.setDefaultValue(mSharedPreferences.getString(
				"download_location", downloadDir));
		DownloadDirectory.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference arg0,
							Object arg1) {
						String new_location = arg1.toString();
						if (new_location.endsWith("/")) {
							new_location = new_location.substring(0,
									new_location.lastIndexOf("/") - 1);
						}
						mEditor.putString("download_location", new_location);
						arg0.setSummary(new_location);
						return false;
					}

				});
		DownloadDirectory.setEnabled(true);
		DownloadDirectory.setLayoutResource(R.layout.pref);
		Download.addPreference(DownloadDirectory);

		CheckBoxPreference FastDownloadMode = new CheckBoxPreference(this);
		FastDownloadMode.setTitle(getText(R.string.settings_fast_dls));
		FastDownloadMode.setSummary(getText(R.string.settings_fast_dls_sum));
		FastDownloadMode.setChecked(mSharedPreferences.getBoolean(
				"fast_download_mode", false));
		FastDownloadMode.setEnabled(true);
		FastDownloadMode.setLayoutResource(R.layout.pref);
		FastDownloadMode.setWidgetLayoutResource(R.layout.pref_widget_checkbox);
		FastDownloadMode.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference arg0) {
						mEditor.putBoolean("fast_download_mode",
								((CheckBoxPreference) arg0).isChecked());
						return false;
					}
				});
		Download.addPreference(FastDownloadMode);

		Preference NoOfConnections = new Preference(this);
		NoOfConnections.setTitle(getText(R.string.settings_connections));
		NoOfConnections.setSummary(getText(R.string.settings_connections_des));
		NoOfConnections.setEnabled(true);
		NoOfConnections.setLayoutResource(R.layout.pref);
		NoOfConnections.setOnPreferenceClickListener(new Connections(mContext,
				mSharedPreferences.getInt("no_of_connections", 8), mEditor));
		Download.addPreference(NoOfConnections);

		PreferenceCategory Cache = new PreferenceCategory(this);
		Cache.setTitle(getText(R.string.CACHE));
		PrefScreen.addPreference(Cache);

		Preference ClearCache = new Preference(this);
		ClearCache.setTitle(getText(R.string.settings_clear_dl_cache));
		ClearCache.setSummary(getText(R.string.settings_clear_dl_cache_sum));
		ClearCache.setEnabled(true);
		ClearCache.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						new ClearDownloadCache(mContext);
						return false;
					}
				});
		ClearCache.setLayoutResource(R.layout.pref);
		Cache.addPreference(ClearCache);

		setPreferenceScreen(PrefScreen);

	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
		mEditor.commit();

	}

}