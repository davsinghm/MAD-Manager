package co.madteam.madmanager.settings;

import java.io.File;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import co.madteam.madmanager.R;
import co.madteam.madmanager.dm.DownloadManager;

public class ClearDownloadCache {
	
	Context mContext;
	
	ClearDownloadCache(Context context) {
		mContext = context;
		clearTemporaryFiles();
	}

	private void clearTemporaryFiles() {
		new AsyncTask<String, String, String>() {

			@Override
			protected void onPostExecute(String result) {
				Toast.makeText(mContext,
						mContext.getText(R.string.temp_files_cleared),
						Toast.LENGTH_SHORT).show();
			}

			@Override
			protected String doInBackground(String... arg) {

				String downloadDirectory = DownloadManager.downloadDirectory(mContext);
				
				File dir = new File(downloadDirectory);
				if (dir.exists() && dir.isDirectory()) {
					for (File file : dir.listFiles()) {
						if (file.getName().endsWith(".tmp")) {
							file.delete();
						}
					}
				}

				File tempDir = new File(downloadDirectory + "/temp");
				if (tempDir.exists() && tempDir.isDirectory()) {
					for (File file : tempDir.listFiles()) {
						file.delete();
					}
				}

				return null;
			}

		}.execute();
	}
}
