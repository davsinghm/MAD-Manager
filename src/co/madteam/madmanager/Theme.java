package co.madteam.madmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Theme {

	public static boolean isActionBarLight(Context context) {
		if (getTheme(context) == R.style.Theme_Light) {
			return true;
		}
		return false;
	}
	
	public static boolean isThemeLight(Context context) {
		if (getTheme(context) == R.style.Theme_Dark) {
			return false;
		}
		return true;
	}
	
	public static int getTheme(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		int theme = sharedPreferences.getInt("theme", 1);
		switch (theme) {
		case 0:
			return R.style.Theme_Dark;
		case 1:
			return R.style.Theme_Light;
		case 2: 
			return R.style.Theme_Light_DarkActionBar;
		}
		return R.style.Theme_Dark;
	}
}
