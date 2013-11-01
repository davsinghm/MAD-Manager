package co.madteam.madmanager;

import java.io.File;

import co.madteam.madmanager.utilities.MadEnvironment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;

public class FlashBaseband extends AsyncTask<String, String, String> {

	private ProgressDialog mProgressDialog;

	@Override
	protected void onPreExecute() {
		mProgressDialog = new ProgressDialog(SelectFilePref.mContext);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog
				.setMessage(SelectFilePref.mContext.getText(R.string.preparing));
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	@Override
	protected String doInBackground(String... arg) {

		String file = arg[0];

		File baseband = new File(file);

		int DeviceID = Devices.getID();

		switch (DeviceID) {

		case 0:

			return "not_supported";

		case Devices.GALAXY5:

			if (baseband.exists()) {

				buildScriptGalaxy5(baseband);

			} else {
				return "error";
			}
			break;
			
			
		case Devices.GALAXY_MINI:

			if (baseband.exists()) {

				buildScriptGalaxy5(baseband);

			} else {
				return "error";
			}
			break;
		}

		return "";

	}
	
	
	private void buildScriptGalaxy5(File file) {

		String baseband = file.getAbsolutePath()
				.replace(Environment.getExternalStorageDirectory().getPath(), "/sdcard")
				.replace("/mnt", "");
		
		if (MadEnvironment.getExternalSDCardPath() != null) {
			baseband = baseband.replace(MadEnvironment.getExternalSDCardPath(), "/emmc");
		}
		
		publishProgress(SelectFilePref.mContext
				.getText(R.string.copying_needed_files).toString());

		Assets.copyFile(SelectFilePref.mContext, "redbend_ua");

		String[] copyfiles = new String[4];
		
		copyfiles[0] = "rm -rf /cache/recovery;";
		copyfiles[1] = "mkdir /cache/recovery;";
		copyfiles[2] = "cat /data/data/co.madteam.madmanager/files/redbend_ua > '/cache/recovery/redbend_ua';";		
		copyfiles[3] = "chmod 777 /cache/recovery/redbend_ua;";

		Command.s(copyfiles);

		publishProgress(SelectFilePref.mContext
				.getText(R.string.generating_command).toString());

		String flash = "run_program(\"/cache/recovery/redbend_ua\", \"restore\", \""
				+ baseband
				+ "\", \"/dev/block/bml4\");";

		String[] cmd = new String[16];
		cmd[0] = "echo 'ui_print(\" \");' > /cache/recovery/extendedcommand;";
		cmd[1] = "echo 'ui_print(\"" + Command.appSign(SelectFilePref.mContext) + "\");' >> /cache/recovery/extendedcommand;";
		cmd[2] = "echo 'ui_print(\" \");' >> /cache/recovery/extendedcommand;";
		cmd[3] = "echo 'ui_print(\"-- Flashing: " + baseband + "\");' >> /cache/recovery/extendedcommand;";
		cmd[4] = "echo 'ui_print(\"Flashing amss...\");' >> /cache/recovery/extendedcommand;";
		cmd[5] = "echo '" + flash + "' >> /cache/recovery/extendedcommand;";
		cmd[6] = "echo 'ui_print(\" \");' >> /cache/recovery/extendedcommand;";
		cmd[7] = "echo 'ui_print(\"Flash complete!\");' >> /cache/recovery/extendedcommand;";
		cmd[8] = "echo 'run_program(\"/cache/recovery/reboot\");' >> /cache/recovery/extendedcommand;";
		cmd[9] = "echo '#!/sbin/busybox sh' > /cache/recovery/reboot;";
		cmd[10] = "echo 'rm /cache/recovery/reboot' >> /cache/recovery/reboot;";
		cmd[11] = "echo 'rm /cache/recovery/redbend_ua' >> /cache/recovery/reboot;";
		cmd[12] = "echo 'rm /cache/recovery/extendedcommand' >> /cache/recovery/reboot;";
		cmd[13] = "echo 'reboot' >> /cache/recovery/reboot;";
		cmd[14] = "chmod 777 /cache/recovery/reboot;";
		cmd[15] = "chmod 777 /cache/recovery/extendedcommand;";

		Command.s(cmd);

		publishProgress(SelectFilePref.mContext
				.getText(R.string.rebooting_into_recovery).toString());

		Command.line("reboot recovery");
		Command.reboot(SelectFilePref.mContext, "recovery");

	}

	@Override
	protected void onProgressUpdate(String... progress) {
		mProgressDialog.setMessage(progress[0]);
	}

	@Override
	protected void onPostExecute(String result) {

		mProgressDialog.cancel();

		if (result.equals("error")) {

			AlertDialog.Builder error = new AlertDialog.Builder(
					SelectFilePref.mContext);
			error.setMessage(SelectFilePref.mContext
					.getText(R.string.flash_baseband_failed));

			error.setNegativeButton(SelectFilePref.mContext.getText(R.string.ok), null);
			error.show();
		}

		else if (result.equals("not_supported")) {

			AlertDialog.Builder error = new AlertDialog.Builder(
					SelectFilePref.mContext);
			error.setMessage(SelectFilePref.mContext
					.getText(R.string.flash_baseband_not_sup));

			error.setNegativeButton(SelectFilePref.mContext.getText(R.string.ok), null);
			error.show();
		}

	}

}