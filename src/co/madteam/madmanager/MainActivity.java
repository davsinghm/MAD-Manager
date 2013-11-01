package co.madteam.madmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.LinearLayout;
import android.widget.Toast;
import co.madteam.madmanager.dm.DownloadManager;
import co.madteam.madmanager.dm.NotificationBuilder;
import co.madteam.madmanager.settings.Settings;
import co.madteam.madmanager.utilities.BlurImage;
import co.madteam.madmanager.utilities.MadUtils;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;

public class MainActivity extends SherlockPreferenceActivity {

	private Context mContext;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(getText(R.string.Downloads))
				.setIntent(Download())
				.setIcon(
						Theme.isActionBarLight(getApplicationContext()) ? R.drawable.action_download_light
								: R.drawable.action_download)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add(getText(R.string.settings))
				.setIntent(Settings())
				.setIcon(
						Theme.isActionBarLight(getApplicationContext()) ? R.drawable.action_settings_light
								: R.drawable.action_settings)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add(getText(R.string.about))
				.setIntent(About())
				.setIcon(
						Theme.isActionBarLight(getApplicationContext()) ? R.drawable.action_about_light
								: R.drawable.action_about)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setTheme(Theme.getTheme(this));

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		getSupportActionBar().setHomeButtonEnabled(false);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.pref_screen_gad);

		setSupportProgressBarIndeterminateVisibility(false);

		mContext = this;

		EasyTracker.getInstance().activityStart(this);
	    
		PreferenceScreen PrefScreen = getPreferenceManager()
				.createPreferenceScreen(this);

		PreferenceCategory PrefCat1 = new PreferenceCategory(this);
		PrefCat1.setTitle(getText(R.string.FLASH_RECOVERY));
		PrefScreen.addPreference(PrefCat1);

		Intent IntentDlRecovery = new Intent(this, RecoveryList.class);

		Preference DownloadRecovery = new Preference(this);
		DownloadRecovery.setTitle(getText(R.string.cwmrecovery));
		DownloadRecovery.setSummary(getText(R.string.cwmrecovery_des));
		DownloadRecovery.setEnabled(true);
		DownloadRecovery.setIntent(IntentDlRecovery);
		DownloadRecovery.setLayoutResource(R.layout.pref);
		PrefCat1.addPreference(DownloadRecovery);

		Preference Recovery = new Preference(this);
		Recovery.setTitle(getText(R.string.recovery));
		Recovery.setSummary(getText(R.string.rebootrecovery));
		Recovery.setEnabled(true);
		Recovery.setOnPreferenceClickListener(new RebootRecovery(this));
		Recovery.setLayoutResource(R.layout.pref);
		PrefCat1.addPreference(Recovery);

		/*
		 * 
		 * 		PreferenceCategory PrefCatR = new PreferenceCategory(this);
		PrefCatR.setTitle(getText(R.string.REBOOT));
		PrefScreen.addPreference(PrefCatR);
		
		Preference RebootDownload = new Preference(this);
		RebootDownload.setTitle(getText(R.string.dlmode));
		RebootDownload.setSummary(getText(R.string.dlmode_des));
		RebootDownload.setEnabled(true);
		RebootDownload.setOnPreferenceClickListener(new RebootDownload(this));
		RebootDownload.setLayoutResource(R.layout.pref);
		PrefCatR.addPreference(RebootDownload);

		Preference Reboot = new Preference(this);
		Reboot.setTitle(getText(R.string.reboot));
		Reboot.setSummary(getText(R.string.reboot_des));
		Reboot.setEnabled(true);
		Reboot.setLayoutResource(R.layout.pref);
		Reboot.setOnPreferenceClickListener(new Reboot(this));
		PrefCatR.addPreference(Reboot);
		


		
		*/

		PreferenceCategory PrefCat2 = new PreferenceCategory(this);
		PrefCat2.setTitle(getText(R.string.ROM_UPDATES));
		PrefScreen.addPreference(PrefCat2);
		
		Intent IntentDlCustomROMs = new Intent(this, ReadROMList.class);

		Preference DownloadCustomROMs = new Preference(this);
		DownloadCustomROMs.setTitle(getText(R.string.downloadroms));
		DownloadCustomROMs.setSummary(getText(R.string.downloadroms_des));
		DownloadCustomROMs.setEnabled(true);
		DownloadCustomROMs.setIntent(IntentDlCustomROMs);
		DownloadCustomROMs.setLayoutResource(R.layout.pref);
		PrefCat2.addPreference(DownloadCustomROMs);

		Preference ROMUpdates = new Preference(this);
		ROMUpdates.setLayoutResource(R.layout.pref);
		ROMUpdates.setTitle(getText(R.string.check_for_updates));
		String currentROM = Command.b("getprop ro.mad.version");
		if ((currentROM.length() == 0)) {
			currentROM = getText(R.string.unknown_o).toString();
		}
		;
		ROMUpdates.setSummary(getText(R.string.current_rom) + " " + currentROM);
		ROMUpdates.setEnabled(true);
		ROMUpdates
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference arg0) {
						new CheckUpdate().execute();
						return false;
					}
				});

		PrefCat2.addPreference(ROMUpdates);

		PreferenceCategory PrefCatF = new PreferenceCategory(this);
		PrefCatF.setTitle(getText(R.string.FLASH));
		PrefScreen.addPreference(PrefCatF);

		Intent IntentInstallZip = new Intent(this, FlashUpdateZip.class);

		Preference InstallZip = new Preference(this);
		InstallZip.setTitle(getText(R.string.flash_update));
		InstallZip.setSummary(getText(R.string.flash_update_des));
		InstallZip.setEnabled(true);
		InstallZip.setLayoutResource(R.layout.pref);
		InstallZip.setIntent(IntentInstallZip);
		PrefCatF.addPreference(InstallZip);

		Intent InBaseband = new Intent(this, SelectFilePref.class).putExtra(
				"select", "baseband");

		Preference Baseband = new Preference(this);
		Baseband.setTitle(getText(R.string.flash_baseband));
		Baseband.setSummary(getText(R.string.current_baseband) + " "
				+ Command.b("getprop ril.sw_ver"));
		Baseband.setIntent(InBaseband);
		Baseband.setEnabled(true);
		Baseband.setLayoutResource(R.layout.pref);
		PrefCatF.addPreference(Baseband);

		PreferenceCategory PrefCatBandR = new PreferenceCategory(this);
		PrefCatBandR.setTitle(getText(R.string.BACKUP_AND_RESTORE));
		PrefScreen.addPreference(PrefCatBandR);

		Intent IntentRestore = new Intent(this, Restore.class);

		Preference ROMBackup = new Preference(this);
		ROMBackup.setTitle(getText(R.string.rom_backups));
		ROMBackup.setSummary(getText(R.string.rom_backups_des));
		ROMBackup.setEnabled(true);
		ROMBackup.setLayoutResource(R.layout.pref);
		ROMBackup.setIntent(IntentRestore);
		PrefCatBandR.addPreference(ROMBackup);

		Intent IntentRestoreBB = new Intent(this, RestoreBaseband.class);

		Preference BasebandBackups = new Preference(this);
		BasebandBackups.setTitle(getText(R.string.baseband_backups));
		BasebandBackups.setSummary(getText(R.string.baseband_backups_des));
		BasebandBackups.setEnabled(true);
		BasebandBackups.setLayoutResource(R.layout.pref);
		BasebandBackups.setIntent(IntentRestoreBB);
		PrefCatBandR.addPreference(BasebandBackups);
		
		PreferenceCategory PrefCatUtilities = new PreferenceCategory(this);
		PrefCatUtilities.setTitle(getText(R.string.UTILITES));
		PrefScreen.addPreference(PrefCatUtilities);
		
		Preference OdexROM = new Preference(this);
		OdexROM.setTitle(getText(R.string.odex_rom));
		OdexROM.setSummary(getText(R.string.odex_rom_des));
		OdexROM.setLayoutResource(R.layout.pref);
		OdexROM.setOnPreferenceClickListener(new OdexROM(this));
		PrefCatUtilities.addPreference(OdexROM);

		Preference PartitionSDCard = new Preference(this);
		PartitionSDCard.setTitle(getText(R.string.partsd_activity));
		PartitionSDCard.setSummary(getText(R.string.partsd_activity_des));
		PartitionSDCard.setLayoutResource(R.layout.pref);
		PartitionSDCard.setIntent(new Intent(this, PartitionSdCard.class));
		PrefCatUtilities.addPreference(PartitionSDCard);
		
		PreferenceCategory PrefCat4 = new PreferenceCategory(this);
		PrefCat4.setTitle(getText(R.string.DONATE));
		PrefScreen.addPreference(PrefCat4);

		Intent DonateIntent = new Intent("android.intent.action.VIEW");
		DonateIntent.setData(Uri.parse("http://madteam.co/donate"));
		
		Preference Donate = new Preference(this);
		Donate.setTitle(getText(R.string.donate));
		Donate.setSummary(getText(R.string.donate_des));
		Donate.setEnabled(true);
		Donate.setLayoutResource(R.layout.pref);
		Donate.setIntent(DonateIntent);
		PrefCat4.addPreference(Donate);

		setPreferenceScreen(PrefScreen);

		boolean gotRoot = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean("got_root", false);

		if (!gotRoot) {
			new CheckROOT().execute();
		}

		Command.checkDir(new File(Environment.getExternalStorageDirectory()
				.getPath() + "/madmanager"));
	}

	private Intent About() {
		Intent about = new Intent(getApplicationContext(), About.class);
		return about;
	}

	private Intent Settings() {
		Intent settings = new Intent(getApplicationContext(), Settings.class);
		return settings;
	}

	private Intent Download() {
		Intent downloads = new Intent(getApplicationContext(),
				DownloadManager.class);
		return downloads;
	}

	public class CheckUpdate extends AsyncTask<String, String, String> {

		private String output;

		@Override
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);

		}

		@Override
		protected String doInBackground(String... arg) {

			String romName = Command.b("getprop ro.mad.rom");

			String romVer = Command.b("getprop ro.mad.version");

			if (!MadUtils.isEmpty(romName) && !MadUtils.isEmpty(romVer)) {
				try {
					String versionUrl = "http://www.2-si.net/_roms/?do=json&sa=verchk&rom=";
					URL readJSON = new URL(versionUrl + romName);
					URLConnection tc = readJSON.openConnection();

					BufferedReader br = new BufferedReader(
							new InputStreamReader(tc.getInputStream()));
					String line = br.readLine();

					if (line == null) {
						output = getText(R.string.no_update_avail).toString();
						return output;

					}
					JSONObject joMain = new JSONObject(line);
					JSONObject jo = joMain.getJSONObject("version");

					String currentRom = romVer.replace(" ", "");
					String LatestRom = jo.getString("ver").replace(" ", "");

					double current_rom = 0;
					double latest_rom = 0;

					try {
						current_rom = Double.parseDouble(currentRom);
						latest_rom = Double.parseDouble(LatestRom);
					} catch (NumberFormatException e) {
					}

					if (latest_rom > current_rom) {

						JSONObject jo2 = joMain.getJSONObject("rom");

						String name = jo2.getString("name");
						String filename = jo2.getString("filename");
						String url = jo2.getString("url");
						String isGooIM = jo2.getString("isGooIM");
						String id = jo2.getString("id");
						String dev = jo2.getString("dev");
						String icon = jo2.getString("icon");
						String md5 = jo2.getString("md5");
						String summary = jo2.getString("summary");
						String rateCount = jo2.getString("rateCount");
						String rating = jo2.getString("rating");
						String downloads = jo2.getString("downloads");

						Bundle extras = new Bundle();

						extras.putString("romname", name);
						extras.putString("filename", filename);
						extras.putString("url", url);
						extras.putString("isGooIM", isGooIM);
						extras.putString("id", id);
						extras.putString("dev", dev);
						extras.putString("icon", icon);
						extras.putString("md5", md5);
						extras.putString("summary", summary);
						extras.putString("rateCount", rateCount);
						extras.putString("rating", rating);
						extras.putString("downloads", downloads);

						Bitmap largeIcon = null;

						boolean useDefaultNotification = PreferenceManager
								.getDefaultSharedPreferences(mContext)
								.getBoolean("use_default_noti", false);

						if (!useDefaultNotification) {
							try {
								BitmapFactory.Options bmOptions = new BitmapFactory.Options();
								bmOptions.inSampleSize = 1;

								URLConnection conn =  new URL(icon).openConnection();

								HttpURLConnection httpConn = (HttpURLConnection) conn;
								httpConn.setRequestMethod("GET");
								httpConn.connect();

								InputStream inputStream = httpConn
										.getInputStream();

								largeIcon = BitmapFactory.decodeStream(
										inputStream, null, bmOptions);
								inputStream.close();

							} catch (IOException e1) {
							}
						}

						Bitmap largeIconScaled = null;
						if (largeIcon != null) {

							largeIconScaled = new BlurImage()
									.createResizedBitmap(
											getResources(), largeIcon,
											100, 100, true);

						}

						Intent newDownload = new Intent(mContext,
								NewDownload.class).putExtras(extras);

						NotificationManager notificationManager = (NotificationManager) getSystemService("notification");

						PendingIntent pendingIntent = PendingIntent
								.getActivity(mContext, 0, newDownload,
										PendingIntent.FLAG_UPDATE_CURRENT);

						NotificationBuilder nb = new NotificationBuilder(
								mContext)
								.setAutoCancel(true)
								.setOngoing(false)
								.setContentIntent(pendingIntent)
								.setContentTitle(name)
								.setSmallIcon(R.drawable.ic_noti_update)
								.setNotiIcon(R.drawable.ic_noti_update)
								.setLargeIcon(largeIconScaled)
								.setContentText(
										getText(R.string.update_avail_des))
								.setWhen(System.currentTimeMillis())
								.setLatestEventTitle(name)
								.setLatestEventText(
										getText(R.string.update_avail_des))
								.setUseDefaultNotification(
										useDefaultNotification)
								.setTicker(getText(R.string.update_avail));

						notificationManager.notify(1, nb.build());

					} else {
						output = getText(R.string.no_update_avail).toString();
					}
					return output;

				} catch (MalformedURLException e) {
					output = getText(R.string.unable_to_connect).toString();
				} catch (IOException e) {
					output = getText(R.string.unable_to_connect).toString();
				} catch (JSONException e) {
					output = getText(R.string.connection_error).toString();
				}
			} else {
				output = getText(R.string.no_update_info_error).toString();
			}

			return output;

		}

		@Override
		protected void onPostExecute(String result) {

			if (result != null) {
				Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
			}
			setSupportProgressBarIndeterminateVisibility(false);

		}

	}

	public class CheckROOT extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);

		}

		@Override
		protected String doInBackground(String... arg) {

			if (Command.isSUed()) {
				if (mContext != null) {
					PreferenceManager.getDefaultSharedPreferences(mContext)
							.edit().putBoolean("got_root", true).commit();
				}
				return null;

			} else if (Command.hasSU()) {
				return "unable_to_get_root";

			} else {
				return "noroot";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null && mContext != null) {
				if (result.equals("noroot")) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(
							mContext)

					.setMessage(getText(R.string.noroot)).setNegativeButton(
							getText(R.string.ok), null);

					dialog.show();
				}
				if (result.equals("unable_to_get_root")) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(
							mContext)

					.setMessage(getText(R.string.unable_to_get_root))
							.setNegativeButton(getText(R.string.ok), null);

					dialog.show();
				}
			}
			setSupportProgressBarIndeterminateVisibility(false);
		}

	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

}
