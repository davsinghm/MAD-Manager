package co.madteam.madmanager;

import android.content.Context;
import android.preference.Preference;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PrefFile extends Preference {

	private int mIcon;
	private String mInfo;

	public PrefFile(Context context) {
		super(context);
		setLayoutResource(R.layout.pref_small_files);
	}

	public void setSupportIcon(int icon) {
		mIcon = icon;
	}
	
	public void setInfo(String info) {
		mInfo = info;
	}
	
	@Override
	public void onBindView(View view) {
		super.onBindView(view);
		if (mIcon != 0) {
			ImageView icon = (ImageView) view.findViewById(R.id.icon);
			icon.setVisibility(ImageView.VISIBLE);
			icon.setImageResource(mIcon);
		}
		
		if (mInfo != null) {
			TextView info = (TextView) view.findViewById(R.id.info);
			info.setText(mInfo);
		}
		
	}
}
