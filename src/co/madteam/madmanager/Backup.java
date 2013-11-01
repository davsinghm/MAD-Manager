package co.madteam.madmanager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import co.madteam.madmanager.utilities.MadEnvironment;
import co.madteam.madmanager.utilities.MadUtils;

import com.actionbarsherlock.view.MenuItem;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

final class Backup implements MenuItem.OnMenuItemClickListener {
	
	Context mContext;
	
	Backup(Context context) {
		mContext = context;
	}

	public boolean onMenuItemClick(MenuItem item) {

		View view = View.inflate(mContext, R.layout.dialog_backup, null);

		/*
		 * TextView space = (TextView) layout.findViewById(R.id.backupspace);
		 * 
		 * StatFs localStatFs2 = new StatFs(
		 * Command.getPrimaryStoragePath(Restore.c)); int n =
		 * localStatFs2.getAvailableBlocks(); int m =
		 * localStatFs2.getBlockSize() / 1024 * n / 1024;
		 * 
		 * space.setText(" " + String.valueOf(m) + " MB");
		 * 
		 * if (m <= 300) { space.setTextColor(0xffff0000); } else if ((m <= 400)
		 * && (m > 300)) { space.setTextColor(0xffFF2400); } else if ((m <= 500)
		 * && (m > 400)) { space.setTextColor(0xffFF6600); } else if ((m <= 600)
		 * && (m > 500)) { space.setTextColor(0xffffff00); } else if ((m <= 700)
		 * && (m > 600)) { space.setTextColor(0xffD1E231); } else if ((m <= 800)
		 * && (m > 700)) { space.setTextColor(0xff99cc32); } else if (m > 800) {
		 * space.setTextColor(0xff00ff00); } else {
		 * space.setTextColor(0xffffffff); }
		 * 
		 * String state = Environment.getExternalStorageState(); if
		 * (!Environment.MEDIA_MOUNTED.equals(state)) { space.setText(" " +
		 * Restore.c.getText(R.string.unmounted));
		 * space.setTextColor(0xffffffff);
		 * 
		 * }
		 */
		String str2 = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss")
				.format(Calendar.getInstance().getTime());

		final EditText edittext = (EditText) view
				.findViewById(R.id.backupedit);
		edittext.setText(str2);
		edittext.selectAll();

		final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

		builder.setView(view);

		builder.setTitle(mContext.getText(R.string.create_backup));
		builder.setPositiveButton(mContext.getText(R.string.ok),
				new OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {

						String backupName = getBackupName(edittext.getText()
								.toString().trim());

						if (!MadUtils.isEmpty(backupName)) {

							String[] cmd = new String[7];

							cmd[0] = "rm -rf /cache/recovery;";
							cmd[1] = "mkdir /cache/recovery;";
							cmd[2] = "echo 'ui_print(\" \");' > /cache/recovery/extendedcommand;";
							cmd[3] = "echo 'ui_print(\""
									+ Command.appSign(mContext)
									+ "\");' >> /cache/recovery/extendedcommand;";
							cmd[4] = "echo 'ui_print(\" \");' >> /cache/recovery/extendedcommand;";
							cmd[5] = "echo 'backup_rom(\""
									+ MadEnvironment.getDefaultBackupPath(mContext)
									+ "/clockworkmod/backup/"
									+ backupName
									+ "/\");' >> '/cache/recovery/extendedcommand';";
							cmd[6] = "reboot recovery";
							Command.s(cmd);
							Command.reboot(mContext, "recovery");
						} else {
							Toast.makeText(mContext,
									mContext.getText(R.string.invalidname),
									Toast.LENGTH_LONG).show();
						}
					}

				});
		builder.setNegativeButton(mContext.getText(R.string.cancel), null);
		builder.show();

		return false;
	}

	private String getBackupName(String str) {

		if (str.length() > 0) {

			StringBuilder builder = new StringBuilder();

			String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.-_";

			char[] allowedList = allowed.toCharArray();
			int allowedLength = allowedList.length;

			boolean lastWasCharacter = false;

			for (char c : str.toCharArray()) {
				int i = 0;
				for (char a : allowedList) {
					i += 1;
					if (a == c) {
						builder.append(c);
						lastWasCharacter = true;
						break;
					} else if (i == allowedLength && lastWasCharacter) {
						builder.append("_");
						lastWasCharacter = false;
					}
				}
			}

			String backupname = builder.toString();

			if (backupname.length() > 0) {
				return backupname;
			}
		}
		return null;
	}
}
