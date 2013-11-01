package co.madteam.madmanager;

import java.util.Locale;

import co.madteam.madmanager.utilities.MadUtils;
import android.content.Context;
import android.preference.Preference;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class PrefComment extends Preference {

	private float mRating;
	private long mTimestamp;
	private boolean mLightTheme;

	public PrefComment(Context context) {
		super(context);
		setLayoutResource(R.layout.pref_download_comment);

	}

	public void setRating(String rating) {
		if (!MadUtils.isEmpty(rating)) {
			try {
				mRating = Float.parseFloat(rating);
			} catch (NumberFormatException e) {
				mRating = 0;
			}
		}
	}

	public void setTimestamp(String timestamp) {
		String ts1 = timestamp + "000";
		mTimestamp = Long.parseLong(ts1);
	}

	public void setLightTheme(boolean isLight) {
		mLightTheme = isLight;
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);
		RatingBar ratingBar = (RatingBar) view
				.findViewById(R.id.ratingBarComment);
		ratingBar.setRating(mRating);

		TextView timestamp = (TextView) view.findViewById(R.id.cmtTimestamp);

		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
				| DateUtils.FORMAT_ABBREV_TIME;

		String ts1 = String.valueOf(DateUtils.getRelativeTimeSpanString(
				mTimestamp, System.currentTimeMillis(), 0, flags));

		timestamp.setText(ts1.toUpperCase(Locale.US));

		ImageView icon = (ImageView) view.findViewById(R.id.icon);
		icon.setImageResource(mLightTheme ? R.drawable.ic_contact_picture_holo_light
				: R.drawable.ic_contact_picture_holo_dark);

	}

}
