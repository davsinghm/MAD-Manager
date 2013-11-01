package co.madteam.madmanager;

import co.madteam.madmanager.utilities.MadUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.preference.Preference;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class PrefROMDescription extends Preference {

	private Bitmap mBackgroundIcon;
	private Bitmap mRomIcon;
	private String mDevName;
	private String mLastestBuild;
	private String mRateCount;
	private String mDownloads;
	private float mRating;

	public PrefROMDescription(Context context) {
		super(context);
		setLayoutResource(R.layout.pref_rom_description);
	}

	public void setBackgroundIcon(Bitmap icon) {
		mBackgroundIcon = icon;
	}

	public void setRomIcon(Bitmap icon) {
		mRomIcon = icon;
	}

	public void setLastestBuild(String string) {
		if (MadUtils.isEmpty(string)) {
			mLastestBuild = "Unknown";
		} else {
			mLastestBuild = string;
		}
	}

	public void setRateCount(String string) {
		if (MadUtils.isEmpty(string)) {
			mRateCount = "0";
		} else {
			mRateCount = string;
		}
	}

	public void setDownloads(String string) {
		if (MadUtils.isEmpty(string)) {
			mDownloads = "0";
		} else {
			mDownloads = string;
		}
	}

	public void setDevName(String dev) {
		mDevName = dev;
	}

	public void setRating(String rating) {
		if (MadUtils.isEmpty(rating)) {
			mRating = 0;
			return;
		}
		try {
			mRating = Float.parseFloat(rating);
		} catch (NumberFormatException e) {
			mRating = 0;
		}
	}

	@Override
	public void setSummary(CharSequence desc) {
		if (MadUtils.isEmpty(desc)) {
			return;
		} else {
			super.setSummary(Html.fromHtml(desc.toString().replace("\n", "<br>")));
		}
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);

		ImageView bg = (ImageView) view.findViewById(R.id.background);
		if (mBackgroundIcon != null) {
			bg.setImageBitmap(mBackgroundIcon);
			bg.setAlpha(125);
		}

		ImageView icon = (ImageView) view.findViewById(R.id.romicon);
		if (mRomIcon != null) {
			icon.setImageBitmap(mRomIcon);
		}

		if (!MadUtils.isEmpty(mDevName)) {
			TextView devname = (TextView) view.findViewById(R.id.devname);
			devname.setText(mDevName);
		}

		RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBarROM);

		ratingBar.setRating(mRating);

		((TextView) view.findViewById(android.R.id.title)).setSelected(true);

		TextView latestBuild = (TextView) view.findViewById(R.id.lastestBuild);
		latestBuild.setText(mLastestBuild);

		TextView rateCount = (TextView) view.findViewById(R.id.rateCount);
		rateCount.setText("(" + mRateCount + ")");

		TextView downloads = (TextView) view.findViewById(R.id.rldownloads);
		downloads.setText(mDownloads + " "
				+ view.getResources().getText(R.string.downloads));

	}
}