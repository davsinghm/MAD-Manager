package co.madteam.madmanager.dm;

import java.text.DecimalFormat;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import co.madteam.madmanager.R;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadActivity extends SherlockActivity {

	@Override
	protected void onPause() {
		isDestroyed = true;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		isDestroyed = true;

		super.onDestroy();
	}

	@Override
	protected void onResume() {
		isDestroyed = false;
		mStartBytes = -1;
		super.onResume();
	}

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

	private TextView mSpeed_TV;
	private TextView mFilename_TV;
	private TextView mFilesize_TV;
	private TextView mDownloaded_TV;
	private TextView mStatus_TV;
	public static ProgressBar mProgressBar_Main;
	private ProgressBar mProgressBar1;
	private ProgressBar mProgressBar2;
	private ProgressBar mProgressBar3;
	private ProgressBar mProgressBar4;
	private ProgressBar mProgressBar5;
	private ProgressBar mProgressBar6;
	private ProgressBar mProgressBar7;
	private ProgressBar mProgressBar8;
	private TextView mDownloaded_part1_TV;
	private TextView mDownloaded_part2_TV;
	private TextView mDownloaded_part3_TV;
	private TextView mDownloaded_part4_TV;
	private TextView mDownloaded_part5_TV;
	private TextView mDownloaded_part6_TV;
	private TextView mDownloaded_part7_TV;
	private TextView mDownloaded_part8_TV;
	private TextView mDownloaded_part1_info_TV;
	private TextView mDownloaded_part2_info_TV;
	private TextView mDownloaded_part3_info_TV;
	private TextView mDownloaded_part4_info_TV;
	private TextView mDownloaded_part5_info_TV;
	private TextView mDownloaded_part6_info_TV;
	private TextView mDownloaded_part7_info_TV;
	private TextView mDownloaded_part8_info_TV;

	static Context mContext;

	long mDownloaded = 0;
	long mStartBytes = -1;
	boolean isDestroyed = true;

	Handler mHandler;

	private Runnable mDownloadSpeed = new Runnable() {
		public void run() {

			mDownloaded = DownloadService.mDownloaded_long();

			long speed1 = (long) (mDownloaded - mStartBytes);

			long speed2 = speed1 / 1024;
			long speed = speed2 / 2;

			mStartBytes = mDownloaded;

			String unit = " KB/sec";

			if (mStartBytes == -1) {
				mSpeed_TV.setText(0 + unit);
			} else {
				mSpeed_TV.setText(speed + unit);

			}

			if (DownloadService.isCompleted() && !DownloadService.isJoined
					&& !DownloadService.isJoining) {
				new DownloadService.join().execute();

				mSpeed_TV.setText("(unknown)");
			}

			if (DownloadService.isDownloading()) {

				mHandler.postDelayed(this, 2000);
			}

		}
	};

	private Runnable mUpdateTVs = new Runnable() {
		public void run() {

			mFilename_TV.setText(DownloadService.mFilename);
			mFilesize_TV.setText(DownloadService.mFilesize_T);
			mStatus_TV.setText(DownloadService.mStatus_T);

			if (DownloadService.mDownloaded_part2 > 0) {
				mProgressBar2.setVisibility(ProgressBar.VISIBLE);

			}
			if (DownloadService.mDownloaded_part3 > 0) {
				mProgressBar3.setVisibility(ProgressBar.VISIBLE);

			}
			if (DownloadService.mDownloaded_part4 > 0) {
				mProgressBar4.setVisibility(ProgressBar.VISIBLE);

			}
			if (DownloadService.mDownloaded_part5 > 0) {
				mProgressBar5.setVisibility(ProgressBar.VISIBLE);

			}
			if (DownloadService.mDownloaded_part6 > 0) {
				mProgressBar6.setVisibility(ProgressBar.VISIBLE);

			}
			if (DownloadService.mDownloaded_part7 > 0) {
				mProgressBar7.setVisibility(ProgressBar.VISIBLE);

			}
			if (DownloadService.mDownloaded_part8 > 0) {
				mProgressBar8.setVisibility(ProgressBar.VISIBLE);

			}

			mProgressBar1.setProgress(DownloadService.mProgressBar1_int);
			mProgressBar2.setProgress(DownloadService.mProgressBar2_int);
			mProgressBar3.setProgress(DownloadService.mProgressBar3_int);
			mProgressBar4.setProgress(DownloadService.mProgressBar4_int);
			mProgressBar5.setProgress(DownloadService.mProgressBar5_int);
			mProgressBar6.setProgress(DownloadService.mProgressBar6_int);
			mProgressBar7.setProgress(DownloadService.mProgressBar7_int);
			mProgressBar8.setProgress(DownloadService.mProgressBar8_int);

			mDownloaded_part1_TV.setText(DownloadService.mDownloaded_part1_T);
			mDownloaded_part2_TV.setText(DownloadService.mDownloaded_part2_T);
			mDownloaded_part3_TV.setText(DownloadService.mDownloaded_part3_T);
			mDownloaded_part4_TV.setText(DownloadService.mDownloaded_part4_T);
			mDownloaded_part5_TV.setText(DownloadService.mDownloaded_part5_T);
			mDownloaded_part6_TV.setText(DownloadService.mDownloaded_part6_T);
			mDownloaded_part7_TV.setText(DownloadService.mDownloaded_part7_T);
			mDownloaded_part8_TV.setText(DownloadService.mDownloaded_part8_T);

			mDownloaded_part1_info_TV
					.setText(DownloadService.mDownloaded_part1_info_T);
			mDownloaded_part2_info_TV
					.setText(DownloadService.mDownloaded_part2_info_T);
			mDownloaded_part3_info_TV
					.setText(DownloadService.mDownloaded_part3_info_T);
			mDownloaded_part4_info_TV
					.setText(DownloadService.mDownloaded_part4_info_T);
			mDownloaded_part5_info_TV
					.setText(DownloadService.mDownloaded_part5_info_T);
			mDownloaded_part6_info_TV
					.setText(DownloadService.mDownloaded_part6_info_T);
			mDownloaded_part7_info_TV
					.setText(DownloadService.mDownloaded_part7_info_T);
			mDownloaded_part8_info_TV
					.setText(DownloadService.mDownloaded_part8_info_T);

			// Downloaded
			long d1 = DownloadService.mDownloaded_long();
			long t1 = 1;

			if (DownloadService.mTotal != 0) {
				t1 = DownloadService.mTotal;
			}
			long progress = d1 * 100 / t1;

			if (!DownloadService.isJoining) {
				mProgressBar_Main.setProgress((int) progress);
			}

			DecimalFormat decimalFormat = new DecimalFormat("#.##");

			String unit_downloaded = " Bytes";

			double downloaded = mDownloaded;
			double size = downloaded;

			if (size > 1024) {
				size = downloaded / 1024;
				unit_downloaded = " KB";
			}
			if (size > 1024) {
				size = downloaded / 1024 / 1024;
				unit_downloaded = " MB";
			}

			String percent = "(" + progress + "%" + ")";

			mDownloaded_TV.setText(decimalFormat.format(size) + unit_downloaded
					+ " " + percent);
			// ***Download end****//

			if (DownloadService.cancelled) {
				mStatus_TV.setText("Cancelled");
			}

			if (DownloadService.isJoining) {
				((Button) findViewById(R.id.cancel)).setEnabled(false);
			}

			if (!isDestroyed && !DownloadService.isCompleted_Really()) {
				mHandler.postDelayed(this, 500);

			} else {
				finish();
			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.service_downloader);

		mContext = this;

		isDestroyed = false;
		mStartBytes = -1;

		mProgressBar_Main = (ProgressBar) findViewById(R.id.progressBar_main);

		mProgressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		mProgressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
		mProgressBar3 = (ProgressBar) findViewById(R.id.progressBar3);
		mProgressBar4 = (ProgressBar) findViewById(R.id.progressBar4);
		mProgressBar5 = (ProgressBar) findViewById(R.id.progressBar5);
		mProgressBar6 = (ProgressBar) findViewById(R.id.progressBar6);
		mProgressBar7 = (ProgressBar) findViewById(R.id.progressBar7);
		mProgressBar8 = (ProgressBar) findViewById(R.id.progressBar8);

		mSpeed_TV = (TextView) findViewById(R.id.speed);
		mStatus_TV = (TextView) findViewById(R.id.status);
		mFilename_TV = (TextView) findViewById(R.id.filename);
		mFilesize_TV = (TextView) findViewById(R.id.filesize);
		mDownloaded_TV = (TextView) findViewById(R.id.downloaded);

		mDownloaded_part1_TV = (TextView) findViewById(R.id.downloaded_part1);
		mDownloaded_part2_TV = (TextView) findViewById(R.id.downloaded_part2);
		mDownloaded_part3_TV = (TextView) findViewById(R.id.downloaded_part3);
		mDownloaded_part4_TV = (TextView) findViewById(R.id.downloaded_part4);
		mDownloaded_part5_TV = (TextView) findViewById(R.id.downloaded_part5);
		mDownloaded_part6_TV = (TextView) findViewById(R.id.downloaded_part6);
		mDownloaded_part7_TV = (TextView) findViewById(R.id.downloaded_part7);
		mDownloaded_part8_TV = (TextView) findViewById(R.id.downloaded_part8);

		mDownloaded_part1_info_TV = (TextView) findViewById(R.id.downloaded_part1_info);
		mDownloaded_part2_info_TV = (TextView) findViewById(R.id.downloaded_part2_info);
		mDownloaded_part3_info_TV = (TextView) findViewById(R.id.downloaded_part3_info);
		mDownloaded_part4_info_TV = (TextView) findViewById(R.id.downloaded_part4_info);
		mDownloaded_part5_info_TV = (TextView) findViewById(R.id.downloaded_part5_info);
		mDownloaded_part6_info_TV = (TextView) findViewById(R.id.downloaded_part6_info);
		mDownloaded_part7_info_TV = (TextView) findViewById(R.id.downloaded_part7_info);
		mDownloaded_part8_info_TV = (TextView) findViewById(R.id.downloaded_part8_info);

		final LinearLayout details_layout = (LinearLayout) findViewById(R.id.details_layout);

		final Button details = (Button) findViewById(R.id.details);
		details.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (details_layout.getVisibility() == LinearLayout.GONE) {
					details_layout.setVisibility(LinearLayout.VISIBLE);
					details.setText("<< Hide details");
				} else {
					details_layout.setVisibility(LinearLayout.GONE);
					details.setText("Show details >>");
				}
			}

		});

		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DownloadService.cancel();
				finish();
			}

		});

		mFilename_TV.setText(DownloadService.mFilename);
		mStatus_TV.setText("Connecting...");
		mHandler = new Handler();
		mHandler.post(mDownloadSpeed);
		mHandler.post(mUpdateTVs);

	}

}
