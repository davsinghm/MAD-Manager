package co.madteam.madmanager;

import android.content.Context;
import android.preference.Preference;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

public class PrefLinkify extends Preference {

	private TextView Summary;
	private boolean mLinkify = false;

	public PrefLinkify(Context context) {
		super(context);
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);
		if (mLinkify) {
			Summary = (TextView) view.findViewById(android.R.id.summary);
			Linkify.addLinks(Summary, Linkify.EMAIL_ADDRESSES
					| Linkify.WEB_URLS);
		}
	}

	public void setLinkify(boolean linkify) {
		mLinkify = linkify;
	}
}
