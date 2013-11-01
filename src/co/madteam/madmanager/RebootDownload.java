package co.madteam.madmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.Preference;

final class RebootDownload implements Preference.OnPreferenceClickListener {

	Context mContext;
	
	RebootDownload(Context context) {
		mContext = context;
	}

	public final boolean onPreferenceClick(Preference preference) {
		AlertDialog.Builder a = new AlertDialog.Builder(mContext)
				.setTitle(mContext.getText(R.string.dlmode))
				.setMessage(mContext.getText(R.string.dlmode_confirm))
				.setPositiveButton(mContext.getText(R.string.reboot),
						new DialogInterface.OnClickListener() {
							public final void onClick(DialogInterface arg0,
									int arg1) {
								Command.line("reboot download");
								Command.reboot(mContext, "download");
							}
						}).setNegativeButton(mContext.getText(R.string.cancel), null);
		a.show();
		return false;
	}
}
