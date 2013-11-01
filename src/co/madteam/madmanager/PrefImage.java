package co.madteam.madmanager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import co.madteam.madmanager.utilities.BlurImage;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.Preference;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class PrefImage extends Preference {

	private String mIconURL;
	private String mRomName;
	private String mRateCount;
	private String mDescription;
	private ImageView mIcon;
	private String mRating;
	private String mWebpage;
	private Bitmap mBitmap;
	private long mDownloads;
	private boolean mAsyncRunning;

	public PrefImage(Context context) {
		super(context);
		setLayoutResource(R.layout.pref_img);
	}

	public void setIconURL(String icon) {
		mIconURL = icon;
		new LoadIcon().execute();
	}

	public void setRomName(String name) {
		mRomName = name;
	}
	
	public void setWebpage(String url) {
		mWebpage = url;
	}

	public void setDescription(String desc) {
		mDescription = desc;
	}

	public void setRating(String rating) {
		mRating = rating;
	}
	
	public void setRateCount(String rating) {
		mRateCount = rating;
	}

	public void setDownloads(long downloads) {
		mDownloads = downloads;
	}

	public String getRomName() {
		return mRomName;
	}

	public String getWebpage() {
		return mWebpage;
	}
	
	public String getIconURL() {
		return mIconURL;
	}
	
	public String getRateCount() {
		return mRateCount;
	}
	
	public String getRating() {
		return mRating;
	}

	public String getDescription() {
		return mDescription;
	}
	
	public long getDownloads() {
		return mDownloads;
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);

		mIcon = (ImageView) view.findViewById(R.id.PrefIconROM);
		if (mBitmap != null) {
			Bitmap scaled = new BlurImage().createResizedBitmap(view.getResources(), mBitmap, 60, 60, true);
			mIcon.setImageBitmap(scaled);
		} else if (!mAsyncRunning) {
			new LoadIcon().execute();
		}

		RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBarROM);
		float rating = 0;
		try {
			rating = Float.parseFloat(mRating);
		} catch (NumberFormatException e) {
			
		}
		ratingBar.setRating(rating);

		TextView downloads = (TextView) view.findViewById(R.id.rldownloads);
		downloads.setText(mDownloads + " "
				+ view.getContext().getText(R.string.downloads));

	}

	private class LoadIcon extends AsyncTask<Void, Void, Bitmap> {

		@Override
		protected void onPreExecute() {
			mAsyncRunning = true;
		}
		
		@Override
		protected Bitmap doInBackground(Void... arg) {

			if (mBitmap != null) {
				return mBitmap;
			} else {
				BitmapFactory.Options bmOptions;
				bmOptions = new BitmapFactory.Options();
				bmOptions.inSampleSize = 1;

				Bitmap bitmap = LoadImage(mIconURL, bmOptions);

				return bitmap;
			}
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			mBitmap = bitmap;
			mAsyncRunning = false;
			notifyChanged();
		}

		private Bitmap LoadImage(String URL, BitmapFactory.Options options) {
			Bitmap bitmap = null;
			InputStream in = null;
			try {

				in = OpenHttpConnection(URL);
				if (in != null) {
					bitmap = BitmapFactory.decodeStream(in, null, options);
					in.close();

				}
			} catch (IOException e1) {
				return null;
			}
			return bitmap;
		}

		private InputStream OpenHttpConnection(String strURL)
				throws IOException {
			InputStream inputStream = null;
			URL url = new URL(strURL);
			URLConnection conn = url.openConnection();

			try {
				HttpURLConnection httpConn = (HttpURLConnection) conn;
				httpConn.setRequestMethod("GET");
				httpConn.connect();

				inputStream = httpConn.getInputStream();

			} catch (Exception ex) {
				return null;
			}
			return inputStream;
		}

	}

}
