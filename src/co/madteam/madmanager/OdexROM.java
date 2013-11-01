package co.madteam.madmanager;

import java.io.IOException;
import java.io.OutputStreamWriter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.Preference;


final class OdexROM implements Preference.OnPreferenceClickListener {

	Context mContext;
	
	OdexROM(Context context) {
		mContext = context;
	}

	public final boolean onPreferenceClick(Preference preference) {
		AlertDialog.Builder a = new AlertDialog.Builder(mContext)
				.setTitle(mContext.getText(R.string.odex_rom))
				.setMessage(mContext.getText(R.string.confirm_odex_rom))
				.setPositiveButton(mContext.getText(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								generateCommand();
							}
						}).setNegativeButton(mContext.getText(R.string.cancel), null);
		a.show();
		return false;
	}
	
	public void generateCommand() {

		Assets.copyFile(mContext, "zip");
		Assets.copyFile(mContext, "dexopt-wrapper");
		Assets.copyFile(mContext, "zipalign");

		String[] cmd = new String[22];

		cmd[0] = "rm -rf /cache/recovery;";
		cmd[1] = "mkdir /cache/recovery;";
		cmd[2] = "cat /data/data/co.madteam.madmanager/files/zip > '/cache/recovery/zip';";
		cmd[3] = "chmod 777 /cache/recovery/zip;";
		cmd[4] = "cat /data/data/co.madteam.madmanager/files/dexopt-wrapper > '/cache/recovery/dexopt-wrapper';";
		cmd[5] = "chmod 777 /cache/recovery/dexopt-wrapper;";
		cmd[6] = "cat /data/data/co.madteam.madmanager/files/zipalign > '/cache/recovery/zipalign';";
		cmd[7] = "chmod 777 /cache/recovery/zipalign;";
		cmd[8] = "echo 'ui_print(\" \");' > /cache/recovery/extendedcommand;";
		cmd[9] = "echo 'ui_print(\"" + Command.appSign(mContext) + "\");' >> /cache/recovery/extendedcommand;";
		cmd[10] = "echo 'ui_print(\" \");' >> /cache/recovery/extendedcommand;";
		cmd[11] = "echo 'ui_print(\"Wiping dalvik cache...\");' >> '/cache/recovery/extendedcommand';";
		cmd[12] = "echo 'mount(\"/sd-ext\");' >> '/cache/recovery/extendedcommand';";
		cmd[13] = "echo 'mount(\"/data\");' >> '/cache/recovery/extendedcommand';";
		cmd[14] = "echo 'mount(\"/system\");' >> '/cache/recovery/extendedcommand';";
		cmd[15] = "echo 'run_program(\"/sbin/busybox\", \"rm\", \"-rf\", \"/cache/dalvik-cache\");' >> '/cache/recovery/extendedcommand';";
		cmd[16] = "echo 'run_program(\"/sbin/busybox\", \"rm\", \"-rf\", \"/data/dalvik-cache\");' >> '/cache/recovery/extendedcommand';";
		cmd[17] = "echo 'run_program(\"/sbin/busybox\", \"rm\", \"-rf\", \"/sd-ext/dalvik-cache\");' >> '/cache/recovery/extendedcommand';";
		cmd[18] = "echo 'ui_print(\" \");' >> /cache/recovery/extendedcommand;";
		cmd[19] = "echo 'ui_print(\"Odexing ROM...\");' >> /cache/recovery/extendedcommand;";
		cmd[20] = "echo 'run_program(\"/cache/recovery/madmanager\");' >> /cache/recovery/extendedcommand;";
		cmd[21] = "chmod 777 /cache/recovery/extendedcommand;";

		Command.s(cmd);
		
		String[] odex = new String[73];
		odex[0] = "#!/system/bin/sh";
		odex[1] = "if [ -f /tmp/tmp.odex ]";
		odex[2] = "then";
		odex[3] = "	rm /tmp/tmp.odex";
		odex[4] = "fi";
		odex[5] = "BOOTCLASSPATH=/system/framework/core.jar";
		odex[6] = "for framework in bouncycastle.jar ext.jar framework.jar android.policy.jar services.jar core-junit.jar";
		odex[7] = "do";
		odex[8] = "	if [ -f /system/framework/$framework ]";
		odex[9] = "	then";
		odex[10] = "		BOOTCLASSPATH=$BOOTCLASSPATH:/system/framework/$framework";
		odex[11] = "	fi";
		odex[12] = "done";
		odex[13] = "echo BOOTCLASSPATH=$BOOTCLASSPATH";
		odex[14] = "cd /system/framework";
		odex[15] = "for filename in core.jar bouncycastle.jar ext.jar framework.jar android.policy.jar services.jar core-junit.jar";
		odex[16] = "do";
		odex[17] = "	name=`basename $filename .jar`";
		odex[18] = "	if [ -f $filename ]";
		odex[19] = "	then";
		odex[20] = "		if [ ! -f $name.odex ]";
		odex[21] = "		then";
		odex[22] = "			/cache/recovery/dexopt-wrapper $filename /tmp/tmp.odex $BOOTCLASSPATH";
		odex[23] = "			if [ $? -eq 0 ]";
		odex[24] = "			then";
		odex[25] = "				/cache/recovery/zip -d $filename classes.dex";
		odex[26] = "				cp /tmp/tmp.odex $name.odex";
		odex[27] = "				rm /tmp/tmp.odex";
		odex[28] = "				chmod 644 $name.odex";
		odex[29] = "			else";
		odex[30] = "				rm /tmp/tmp.odex";
		odex[31] = "			fi";
		odex[32] = "		fi";
		odex[33] = "	fi";
		odex[34] = "done";
		odex[35] = "cd /system/framework";
		odex[36] = "for filename in *.jar";
		odex[37] = "do";
		odex[38] = "	name=`basename $filename .jar`";
		odex[39] = "	if [ ! -f $name.odex ]";
		odex[40] = "	then";
		odex[41] = "		/cache/recovery/dexopt-wrapper $filename /tmp/tmp.odex $BOOTCLASSPATH";
		odex[42] = "		if [ $? -eq 0 ]";
		odex[43] = "		then";
		odex[44] = "			/cache/recovery/zip -d $filename classes.dex";
		odex[45] = "			cp /tmp/tmp.odex $name.odex";
		odex[46] = "			rm /tmp/tmp.odex";
		odex[47] = "			chmod 644 $name.odex";
		odex[48] = "		else";
		odex[49] = "			rm /tmp/tmp.odex";
		odex[50] = "		fi";
		odex[51] = "	fi";
		odex[52] = "done";
		odex[53] = "cd /system/app";
		odex[54] = "for filename in *.apk";
		odex[55] = "do";
		odex[56] = "	name=`basename $filename .apk`";
		odex[57] = "	if [ ! -f $name.odex ]";
		odex[58] = "	then";
		odex[59] = "		/cache/recovery/dexopt-wrapper $filename /tmp/tmp.odex $BOOTCLASSPATH";
		odex[60] = "		if [ $? -eq 0 ]";
		odex[61] = "		then";
		odex[62] = "			/cache/recovery/zip -d $filename classes.dex";
		odex[63] = "			/cache/recovery/zipalign -f 4 $filename /tmp/$filename.new";
		odex[64] = "			cat /tmp/$filename.new >$filename";
		odex[65] = "			cp /tmp/tmp.odex $name.odex";
		odex[66] = "			rm /tmp/$filename.new";
		odex[67] = "			rm /tmp/tmp.odex";
		odex[68] = "			chmod 644 /system/app/$name.odex";
		odex[69] = "			sleep 1";
		odex[70] = "		fi";
		odex[71] = "	fi";
		odex[72] = "done";
		
		echo(odex);
		
		Command.line("chmod 777 /cache/recovery/madmanager");
		Command.line("reboot recovery");
		Command.reboot(mContext, "recovery");


	}
	
	public void echo(String[] commands) {
		Runtime runtime = Runtime.getRuntime();
		Process proc = null;
		OutputStreamWriter osw = null;
		try {
			proc = runtime.exec("su");
			osw = new OutputStreamWriter(proc.getOutputStream());

			for (int i = 0; i < commands.length; i++) {
				osw.write("echo '" + commands[i] + "' >> '/cache/recovery/madmanager'" + "\n");
			}
			osw.flush();
			osw.close();
			proc.waitFor();

		} catch (InterruptedException e) {
		} catch (IOException e) {
		}
	}

}
