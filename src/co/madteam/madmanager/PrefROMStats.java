package co.madteam.madmanager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.preference.Preference;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

public class PrefROMStats extends Preference {

	private float mRating;
	private String mRateCount;
	private String mDownloads;
	private TextView Summary;
	private String mMD5;
	private boolean mLinkify = false;
	private boolean mMD5Link = false;

	public PrefROMStats(Context context) {
		super(context);
		setLayoutResource(R.layout.pref_download_data_description);
	}

	public void setRating(String rating) {
		if (rating == null) {
			mRating = 0;
			return;
		}
		try {
			mRating = Float.parseFloat(rating);
		} catch (NumberFormatException e) {
			mRating = 0;
		}
	}

	public void setRateCount(String rateCount) {
		if (rateCount != null) {
			mRateCount = rateCount;
		} else {
			mRateCount = "0";
		}
	}

	public void setDownloads(String downloads) {
		if (downloads != null) {
		mDownloads = downloads;
		} else {
			mDownloads = "0";
		}
	}

	public void setLinkify(boolean linkify) {
		mLinkify = linkify;
	}

	public void setMD5Link(boolean md5link, String md5) {
		mMD5Link = md5link;
		mMD5 = md5;
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);

		RatingBar ratingBar = (RatingBar) view
				.findViewById(R.id.ratingBarComment);

		ratingBar.setRating(mRating);

		TextView rateCount = (TextView) view.findViewById(R.id.rateCount);
		rateCount.setText(mRateCount);

		TextView downloads = (TextView) view.findViewById(R.id.downloads);
		downloads.setText(mDownloads + " "
				+ view.getContext().getText(R.string.downloads));

		if (mLinkify) {
			Summary = (TextView) view.findViewById(android.R.id.summary);
			Linkify.addLinks(Summary, Linkify.WEB_URLS);
		}
		if (mMD5Link) {
			Summary = (TextView) view.findViewById(android.R.id.summary);

			TransformFilter mentionFilter = new TransformFilter() {
				public String transformUrl(Matcher match, String url) {
					return mMD5;
				}
			};

			Pattern pattern = Pattern.compile(view.getContext()
					.getText(R.string.copy_md5).toString());
			String scheme = "md5:";
			Linkify.addLinks(Summary, pattern, scheme, null, mentionFilter);

		}
	}
}