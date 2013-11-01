package co.madteam.madmanager;

import co.madteam.madmanager.utilities.MadUtils;
import android.content.Context;
import android.preference.Preference;
import android.view.View;
import android.widget.ImageView;

public class PrefMainScreen extends Preference {

	private int mIcon;
	private int mBackground;

	public PrefMainScreen(Context context) {
		super(context);
		setLayoutResource(R.layout.pref_mainscreen);
	}

	public void setSupportIcon(int res) {
		mIcon = res;
	}

	public void setBackground(int bitmap) {
		mBackground = bitmap;
	}

	@Override
	public void setSummary(CharSequence desc) {
		if (MadUtils.isEmpty(desc)) {
			return;
		} else {
			super.setSummary(desc);
		}
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);

		if (mIcon != 0) {
			ImageView icon = (ImageView) view.findViewById(R.id.icon);
			icon.setImageResource(mIcon);
		}
		if (mBackground != 0) {
			ImageView bg = (ImageView) view.findViewById(R.id.background);
			bg.setImageResource(mBackground);
			bg.setAlpha(100);
		}
	}
}