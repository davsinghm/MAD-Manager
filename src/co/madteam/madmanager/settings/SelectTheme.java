package co.madteam.madmanager.settings;

import co.madteam.madmanager.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.preference.Preference;

final class SelectTheme implements Preference.OnPreferenceClickListener {

	Context mContext;
	SharedPreferences.Editor mEditor;
	int mDefaultValue;
	
	SelectTheme(Context context, int defaultValue, SharedPreferences.Editor editor) {
		mContext = context;
		mDefaultValue = defaultValue;
		mEditor = editor;
	}

	public final boolean onPreferenceClick(Preference preference) {

		CharSequence[] items = { "Dark", "Light", "Light (DarkActionBar)" };

		AlertDialog.Builder choose = new AlertDialog.Builder(mContext);
		choose.setTitle(mContext.getText(R.string.select_theme));
		choose.setSingleChoiceItems(items,
				mDefaultValue,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						mEditor.putInt("theme", arg1);
						mDefaultValue = arg1;
						arg0.dismiss();
					}
				});
		choose.setNegativeButton(mContext.getText(R.string.cancel), null);
		choose.show();
		
		return false;
	}

}
