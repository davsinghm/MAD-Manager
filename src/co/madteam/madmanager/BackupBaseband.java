package co.madteam.madmanager;

import com.actionbarsherlock.view.MenuItem;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

final class BackupBaseband implements MenuItem.OnMenuItemClickListener {

	Context mContext;
	
	BackupBaseband(Context context) {
		mContext = context;
	}

	public boolean onMenuItemClick(MenuItem item) {
		
		AlertDialog.Builder a = new AlertDialog.Builder(mContext)
				.setTitle(mContext.getText(R.string.create_backup))
				.setMessage(
						mContext
								.getText(R.string.backup_baseband_confirm))
				.setPositiveButton(mContext.getText(R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

								new AsyncTask<String, String, String>() {
									ProgressDialog mProgressDialog;

									@Override
									protected void onPreExecute() {

										mProgressDialog = new ProgressDialog(
												mContext);
										mProgressDialog.setIndeterminate(false);
										mProgressDialog.setMessage(mContext
												.getText(R.string.preparing));
										mProgressDialog
												.setProgressStyle(ProgressDialog.STYLE_SPINNER);
										mProgressDialog.setCancelable(false);
										mProgressDialog.show();
									}

									@Override
									protected String doInBackground(
											String... arg0) {

										int DeviceID = Devices.getID();

										switch (DeviceID) {

										case 0:
											return "not_supported";

										case Devices.GALAXY5:
											buildScriptGalaxy5();
											break;

										case Devices.GALAXY_MINI:
											buildScriptGalaxy5();
											break;
										}

										return "";

									}

									private void buildScriptGalaxy5() {
										publishProgress(mContext.getText(
												R.string.copying_needed_files)
												.toString());

										Assets.copyFile(mContext,
												"redbend_ua");

										String[] copyfiles = new String[4];

										copyfiles[0] = "rm -rf /cache/recovery;";
										copyfiles[1] = "mkdir /cache/recovery;";
										copyfiles[2] = "cat /data/data/co.madteam.madmanager/files/redbend_ua > '/cache/recovery/redbend_ua';";
										copyfiles[3] = "chmod 777 /cache/recovery/redbend_ua ;";

										Command.s(copyfiles);

										publishProgress(mContext.getText(
												R.string.generating_command)
												.toString());

										String bname = Command
												.b("getprop ril.sw_ver");

										String dir = "/sdcard/madmanager/backup/"
												+ bname;

										String backup_file = "/cache/recovery/redbend_ua dump /dev/block/bml4 "
												+ dir + "/baseband";

										String[] script = new String[15];

										script[0] = "echo 'ui_print(\" \");' > /cache/recovery/extendedcommand;";
										script[1] = "echo 'ui_print(\"" + Command.appSign(mContext)
												+ "\");' >> /cache/recovery/extendedcommand;";
										script[2] = "echo 'ui_print(\" \");' >> /cache/recovery/extendedcommand;";
										script[3] = "echo 'ui_print(\"Backing up baseband...\");' >> /cache/recovery/extendedcommand;";
										script[4] = "echo 'run_program(\"/cache/recovery/madmanager\");' >> /cache/recovery/extendedcommand;";
										script[5] = "echo '#!/sbin/busybox sh' > /cache/recovery/madmanager;";
										script[6] = "echo '" + "mkdir -p " + dir + "' >> /cache/recovery/madmanager;";
										script[7] = "echo '" + backup_file + "' >> /cache/recovery/madmanager;";
										script[8] = "echo 'rm /cache/recovery/madmanager' >> /cache/recovery/madmanager;";
										script[9] = "echo 'rm /cache/recovery/redbend_ua' >> /cache/recovery/madmanager;";
										script[10] = "echo 'md5=\"$(md5sum " + dir + "/baseband" + ")\"' >> /cache/recovery/madmanager;";
										script[11] = "echo 'echo $md5 > " + dir + "/md5" + "' >> /cache/recovery/madmanager;";
										script[12] = "echo 'reboot' >> /cache/recovery/madmanager;";
										script[13] = "chmod 777 /cache/recovery/madmanager;";
										script[14] = "chmod 777 /cache/recovery/extendedcommand;";

										Command.s(script);
										publishProgress(mContext
												.getText(
														R.string.rebooting_into_recovery)
												.toString());

										Command.line("reboot recovery");
										Command.reboot(mContext, "recovery");

									}

									@Override
									protected void onProgressUpdate(
											String... progress) {
										mProgressDialog.setMessage(progress[0]);
									}

									@Override
									protected void onPostExecute(String result) {

										mProgressDialog.cancel();

										if (result.equals("not_supported")) {

											AlertDialog.Builder error = new AlertDialog.Builder(
													mContext);
											error.setMessage(mContext
													.getText(R.string.flash_baseband_backup_not_sup));

											error.setNegativeButton(
													mContext
															.getText(R.string.ok),
													null);
											error.show();
										}

									}

								}.execute();

							}
						})
				.setNegativeButton(mContext.getText(R.string.no),
						null);
		a.show();
		return false;
	}

}
