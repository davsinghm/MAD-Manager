package co.madteam.madmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.Preference;

final class Reboot implements Preference.OnPreferenceClickListener {

	Context mContext;
	
	Reboot(Context context) {
		mContext = context;
	}

	public final boolean onPreferenceClick(Preference preference) {
		AlertDialog.Builder a = new AlertDialog.Builder(mContext)
				.setTitle(mContext.getText(R.string.reboot))
				.setMessage(mContext.getText(R.string.reboot_confirm))
				.setPositiveButton(mContext.getText(R.string.reboot),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								Command.line("reboot");
								Command.reboot(mContext, null);
							}
						}).setNegativeButton(mContext.getText(R.string.cancel), null);
		a.show();
		return false;
	}

}
