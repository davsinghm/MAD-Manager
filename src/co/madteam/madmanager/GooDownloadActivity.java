package co.madteam.madmanager;

import co.madteam.madmanager.dm.Download;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.analytics.tracking.android.EasyTracker;
import android.os.Bundle;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Context;
import android.graphics.Bitmap;

public class GooDownloadActivity extends SherlockActivity {

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

	String FILENAME;
	int ID;
	String MD5;
	Bitmap ICON;
	Bundle EXTRAS;
	WebView webView;
	Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setTheme(Theme.getTheme(this));

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);

		setSupportProgressBarIndeterminateVisibility(false);

		setContentView(R.layout.activity_web);
		
		mContext = this;

		EasyTracker.getInstance().activityStart(this);

		String getURL = getIntent().getStringExtra("url");

		FILENAME = getIntent().getStringExtra("filename");
		ID = getIntent().getIntExtra("id", 0);
		MD5 = getIntent().getStringExtra("md5");
		ICON = getIntent().getParcelableExtra("icon");
		EXTRAS = getIntent().getBundleExtra("extras");

		webView = (WebView) findViewById(R.id.gwebview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setSupportMultipleWindows(false);

		if (getURL.contains("goo.im")) {
			webView.loadUrl(getURL + "&android=true");
		}

		else {
			webView.loadUrl(getURL);
		}

		WebViewClient webViewClient = new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				setSupportProgressBarIndeterminateVisibility(true);

			}

			@Override
			public void onPageFinished(WebView paramWebView, String paramString) {
				super.onPageFinished(paramWebView, paramString);

				setSupportProgressBarIndeterminateVisibility(false);

			}

		};
		webView.setWebViewClient(webViewClient);

		webView.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {

				Download download = new Download(mContext);
				download.setFilename(FILENAME);
				download.setMD5(MD5);
				download.setURL(url);
				download.setRomID(ID);
				download.setIcon(ICON);
				download.setExtras(EXTRAS);
				download.startDownload();
				
				finish();

			}
		});
	}

	@Override
	public void onStop() {
		super.onStop();
		webView.stopLoading();
		EasyTracker.getInstance().activityStop(this);
	}

}