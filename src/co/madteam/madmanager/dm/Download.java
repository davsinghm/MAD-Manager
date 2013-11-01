package co.madteam.madmanager.dm;

import co.madteam.madmanager.utilities.MadUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

public class Download {

	private Context mContext;
	private String mFilename;
	private String mURL;
	private String mMD5;
	private int mRomID;
	private Bitmap mIcon;
	private Bundle mExtras;

	public Download(Context context) {
		mContext = context;
	}

	public Download setFilename(String filename) {
		mFilename = filename;
		return this;
	}

	public Download setURL(String url) {
		mURL = url;
		return this;
	}

	public Download setMD5(String md5) {
		if (!MadUtils.isEmpty(md5)) {
			mMD5 = md5;
		}
		return this;
	}

	public Download setRomID(int romid) {
		mRomID = romid;
		return this;
	}

	public Download setIcon(Bitmap icon) {
		mIcon = icon;
		return this;
	}

	public Download setExtras(Bundle extras) {
		mExtras = extras;
		return this;
	}

	public void startDownload() {

		Bundle bundle = new Bundle();
		bundle.putString("filename", mFilename);
		bundle.putString("md5", mMD5);
		bundle.putString("url", mURL);
		bundle.putInt("romid", mRomID);
		bundle.putParcelable("icon", mIcon);
		bundle.putBundle("extras", mExtras);
		
		DownloadFileService.addDownload(mContext, bundle);
		
	}

}
