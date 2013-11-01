package co.madteam.madmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.AlarmManager;
import android.app.PendingIntent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		if (sharedPreferences.getBoolean("check_for_updates", true)) {

			long freq = frequency(sharedPreferences.getInt("update_check_freq", 4));
			
			Intent cIntent = new Intent(context, CheckUpdateReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					0, cIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService("alarm");
			alarmManager.cancel(pendingIntent);
			long currentMillis = System.currentTimeMillis();

			alarmManager.setRepeating(1, currentMillis, freq,
					pendingIntent);
		}
		return;

	}

	private long frequency(int value) {

		switch (value) {
		case 0:
			return 30 * 60 * 1000;
		case 1:
			return 60 * 60 * 1000;
		case 2:
			return 6 * 60 * 60 * 1000;
		case 3:
			return 12 * 60 * 60 * 1000;
		case 4:
			return 24 * 60 * 60 * 1000;
		case 5:
			return 48 * 60 * 60 * 1000;
		default: return 24 * 60 * 60 * 1000;
		}
		
	}

}