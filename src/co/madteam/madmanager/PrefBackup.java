package co.madteam.madmanager;

import android.content.Context;
import android.preference.Preference;
import android.view.View;
import android.widget.TextView;

public class PrefBackup extends Preference {

	public String mInfo;
	public String mSize;

	public PrefBackup(Context context) {
		super(context);
		setLayoutResource(R.layout.pref_backup);
	}

	public void setInfo(String info) {
		mInfo = info;
	}

	public void setSize(String size) {
		mSize = size;
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);

		TextView Info = (TextView) view.findViewById(R.id.pref_backup_info);
		Info.setText(mInfo);

		TextView Size = (TextView) view.findViewById(R.id.pref_backup_size);
		Size.setText(mSize);

	}

}
