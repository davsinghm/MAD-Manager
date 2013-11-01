package co.madteam.madmanager.settings;

import co.madteam.madmanager.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.preference.Preference;

final class Connections implements Preference.OnPreferenceClickListener {

	Context mContext;
	SharedPreferences.Editor mEditor;
	int mDefaultValue;
	
	Connections(Context context, int defaultValue, SharedPreferences.Editor editor) {
		mContext = context;
		mDefaultValue = defaultValue;
		mEditor = editor;
	}

	public final boolean onPreferenceClick(Preference preference) {


		CharSequence[] items = { "2", "3", "4", "6", "8", "16" };
		int selected = 2;
		switch (mDefaultValue) {
		case 2:
			selected = 0;
			break;
		case 3:
			selected = 1;
			break;
		case 4:
			selected = 2;
			break;
		case 6:
			selected = 3;
			break;
		case 8:
			selected = 4;
			break;
		case 16:
			selected = 5;
			break;
		}

		AlertDialog.Builder choose = new AlertDialog.Builder(mContext);
		choose.setTitle(mContext.getText(R.string.settings_connections));
		choose.setSingleChoiceItems(items, selected,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0,
							int arg1) {
						int which = 2;
						switch (arg1) {
						case 0:
							which = 2;
							break;
						case 1:
							which = 3;
							break;
						case 2:
							which = 4;
							break;
						case 3:
							which = 6;
							break;
						case 4:
							which = 8;
							break;
						case 5:
							which = 16;
							break;
						}
						
						mEditor.putInt("no_of_connections", which);
						new ClearDownloadCache(mContext);
						mDefaultValue = arg1;
						arg0.dismiss();
					}
				});
		choose.setNegativeButton(mContext.getText(R.string.cancel), null);
		choose.show();
		
		return false;
	}

}
