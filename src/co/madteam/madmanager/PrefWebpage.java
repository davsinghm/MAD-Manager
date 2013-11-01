package co.madteam.madmanager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.view.View;
import android.widget.ImageView;

public class PrefWebpage extends Preference {

	public PrefWebpage(Context context) {
		super(context);
		setLayoutResource(R.layout.pref_download_webpage);
	}

	public void setURL(String url, Uri uri) {		
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.setData(uri);
		setIntent(intent);
		setSummary(url);
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);
		ImageView icon = (ImageView) view.findViewById(R.id.icon);
		icon.setVisibility(ImageView.VISIBLE);
		if (Theme.isThemeLight(view.getContext())) {
			icon.setImageResource(R.drawable.action_webpage_light);
		} else {
			icon.setImageResource(R.drawable.action_webpage_dark);
		}
	}
}
