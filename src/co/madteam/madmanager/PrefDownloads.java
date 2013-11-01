package co.madteam.madmanager;

import android.content.Context;
import android.preference.Preference;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PrefDownloads extends Preference {
	
	private String mInfo;
	private String mDate;
	private boolean mSelected;
	private int mIcon;

	public PrefDownloads(Context context) {
		super(context);
		setLayoutResource(R.layout.pref_downloads);
	}

	public void setInfo(String info) {
		mInfo = info;
	}
	
	public void setDate(String date) {
		mDate = date;
	}
	
	public void setSupportIcon(int res) {
		mIcon = res;
	}
	
	public void setSelected(boolean bool) {
		mSelected = bool;
		notifyChanged();
	}
	
	public boolean isSelected() {
		return mSelected;
	}
	
    @Override
    protected void onClick() {
        super.onClick();
        setSelected(!mSelected);
    }
	
	@Override
	public void onBindView(View view) {
		super.onBindView(view);

		if (mSelected) {
			view.setBackgroundColor(0x6033b5e5);
		} else {
			view.setBackgroundColor(0x00000000);
		}
		
		TextView info = (TextView) view
				.findViewById(R.id.info);
		info.setText(mInfo);
		
		TextView date = (TextView) view
				.findViewById(R.id.date);
		date.setText(mDate);
		
		ImageView icon = (ImageView) view
				.findViewById(R.id.icon);
		icon.setImageResource(mIcon);
		

	}
}