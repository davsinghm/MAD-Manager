package co.madteam.madmanager;

import java.io.File;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Environment;

public class FlashRecovery extends AsyncTask<String, String, String> {

	private ProgressDialog mProgressDialog;
	
	private void flash_image(File recovery, String flash_image, String partition) {

		String[] cmd = new String[4];
		cmd[0] = "cat /data/data/co.madteam.madmanager/files/" + flash_image + " > /cache/flash_image";
		cmd[1] = "chmod 777 /cache/flash_image";
		cmd[2] = "/cache/flash_image " + partition + " "
				+ recovery.getAbsolutePath();
		cmd[3] = "rm /cache/flash_image";
		Command.s(cmd);
	}

	@Override
	protected void onPreExecute() {
		mProgressDialog = new ProgressDialog(RecoveryList.mContext);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMessage(RecoveryList.mContext.getText(R.string.preparing));
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	@Override
	protected String doInBackground(String... arg) {

		String file = arg[0];
		String md5 = arg[1];

		File recovery = new File(Environment.getExternalStorageDirectory().getPath() + "/madmanager/recovery/" + file);

		File flash_image = new File("/data/data/co.madteam.madmanager/files/" + Devices.getDeviceName());
		
		if (!flash_image.exists() || !recovery.exists() || recovery.length() == 0 
				|| !(MD5Checksum.verifyMD5Checksum(
						recovery.getAbsolutePath(), md5))) {
			return "error";
		}
		
		switch (Devices.getID()) {

		case Devices.GALAXY5:

				publishProgress();
				
				flash_image(recovery, Devices.getDeviceName(), "recoveryonly");

				return "success";

		case Devices.GALAXY_MINI:

				publishProgress();
				
				flash_image(recovery, Devices.getDeviceName(), "recoveryonly");

				return "success";

		case Devices.OPTIMUS_CHIC:

				publishProgress();

				flash_image(recovery, "flash_image", "recoveryonly");
				
				return "success";
			
		case Devices.GALAXY_FIT:

				publishProgress();

				flash_image(recovery, Devices.getDeviceName(), "recoveryonly");

				return "success";

		}

		return "not_supported";
		
	}

	@Override
	protected void onProgressUpdate(String... progress) {
		mProgressDialog.setMessage(RecoveryList.mContext.getText(R.string.flashing));
	}

	@Override
	protected void onPostExecute(String result) {
		mProgressDialog.cancel();

		if (result.equals("success")) {
			AlertDialog.Builder success = new AlertDialog.Builder(
					RecoveryList.mContext);
			success.setMessage(RecoveryList.mContext
					.getText(R.string.flash_cwm_success));
			success.setPositiveButton(
					RecoveryList.mContext.getText(R.string.reboot_recovery),
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Command.line("reboot recovery");
							Command.reboot(RecoveryList.mContext, "recovery");
						}
					});
			success.setNegativeButton(RecoveryList.mContext.getText(R.string.done),
					null);
			success.show();

		}

		else if (result.equals("error")) {

			AlertDialog.Builder error = new AlertDialog.Builder(RecoveryList.mContext);
			error.setMessage(RecoveryList.mContext.getText(R.string.flash_cwm_failed));
			error.setNegativeButton(RecoveryList.mContext.getText(R.string.ok), null);
			error.show();
		}

		else if (result.equals("not_supported")) {

			AlertDialog.Builder error = new AlertDialog.Builder(RecoveryList.mContext);
			error.setMessage(RecoveryList.mContext.getText(R.string.flash_cwm_not_sup));
			error.setNegativeButton(RecoveryList.mContext.getText(R.string.ok), null);
			error.show();
		}

	}

}