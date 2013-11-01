package co.madteam.madmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.widget.Toast;

public class CopyMD5 extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();

		String md5 = intent.getDataString().replace("md5:", "").replace(" ", "");

		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		clipboard.setText(md5);
		Toast.makeText(getApplicationContext(), getText(R.string.md5_copied), 1)
				.show();
		finish();
	}
}