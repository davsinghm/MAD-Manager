package co.madteam.madmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.Preference;

final class RebootRecovery implements Preference.OnPreferenceClickListener {

	Context mContext;
	
	RebootRecovery(Context context) {
		mContext = context;
	}
	
	public final boolean onPreferenceClick(Preference preference) {
		AlertDialog.Builder a = new AlertDialog.Builder(mContext)
				.setTitle(mContext.getText(R.string.recovery))
				.setMessage(
						mContext.getText(R.string.rebootrecovery_confirm))
				.setPositiveButton(mContext.getText(R.string.reboot),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								Command.line("reboot recovery");
								Command.reboot(mContext, "recovery");
							}
						}).setNegativeButton(mContext.getText(R.string.cancel), null);
		a.show();
		return false;
	}

}
