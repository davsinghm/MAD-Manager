package co.madteam.madmanager;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import co.madteam.madmanager.R;
import co.madteam.madmanager.utilities.MadUtils;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class PartitionSdCard extends SherlockActivity {

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			super.onBackPressed();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setTheme(Theme.getTheme(this));

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_partition);

		final EditText extSizeET = (EditText) findViewById(R.id.extSize);

		final EditText swapSizeET = (EditText) findViewById(R.id.swapSize);

		final RadioGroup extGroup = (RadioGroup) findViewById(R.id.extGroup);

		Button partition = (Button) findViewById(R.id.button);
		partition.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				String extSize = extSizeET.getText().toString();
				if (MadUtils.isEmpty(extSize)) {
					extSize = "0";
				}
				String swapSize = swapSizeET.getText().toString();
				if (MadUtils.isEmpty(swapSize)) {
					swapSize = "0";
				}
				String partition = "sdparted -es " + extSize + " -ss "
						+ swapSize + " -efs "
						+ ext(extGroup.getCheckedRadioButtonId()) + " -s";

				String[] script = new String[12];
				script[0] = "rm -rf /cache/recovery;";
				script[1] = "mkdir /cache/recovery;";
				script[2] = "echo 'ui_print(\" \");' > /cache/recovery/extendedcommand;";
				script[3] = "echo 'ui_print(\""
						+ Command.appSign(getApplicationContext())
						+ "\");' >> /cache/recovery/extendedcommand;";
				script[4] = "echo 'ui_print(\" \");' >> /cache/recovery/extendedcommand;";
				script[5] = "echo 'ui_print(\"Partitioning SD card...\");' >> /cache/recovery/extendedcommand;";
				script[6] = "echo 'run_program(\"/cache/recovery/madmanager\");' >> /cache/recovery/extendedcommand;";
				script[7] = "echo '#!/sbin/busybox sh' > /cache/recovery/madmanager;";
				script[8] = "echo '" + partition
						+ "' >> /cache/recovery/madmanager;";
				script[9] = "chmod 777 /cache/recovery/madmanager;";
				script[10] = "chmod 777 /cache/recovery/extendedcommand;";
				script[11] = "reboot recovery";
				
				Command.s(script);
				Command.reboot(getApplicationContext(), "recovery");
				
			}

			private String ext(int id) {

				switch (id) {
				case R.id.ext2:
					return "ext2";
				case R.id.ext3:
					return "ext3";
				case R.id.ext4:
					return "ext4";
				}

				return "ext4";
			}

		});

	}

}
