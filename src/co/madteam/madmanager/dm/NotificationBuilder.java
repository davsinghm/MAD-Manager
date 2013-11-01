package co.madteam.madmanager.dm;

import co.madteam.madmanager.R;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

public class NotificationBuilder {

	private Context mContext;
	private long mWhen;
	private int mSmallIcon;
	private int mNotiIcon;
	private int mNumber;
	private CharSequence mContentTitle;
	private CharSequence mContentText;
	private CharSequence mContentInfo;
	private CharSequence mTickerText;
	private CharSequence mSubText;
	private CharSequence mLatestEventTitle;
	private CharSequence mLatestEventText;
	private PendingIntent mContentIntent;
	private Bitmap mLargeIcon;
	private int mFlags;
	private int mProgressMax;
	private int mProgress;
	private boolean mProgressIndeterminate;
	private boolean mShowWhen = true;
	private boolean mOnGoing;
	private boolean mAutoCancel;
	private boolean mUseDefaultNotification;

	public NotificationBuilder(Context context) {
		mContext = context;
	}

	public NotificationBuilder setWhen(long when) {
		mWhen = when;
		return this;
	}

	public NotificationBuilder setShowWhen(boolean show) {
		mShowWhen = show;
		return this;
	}

	public NotificationBuilder setSmallIcon(int icon) {
		mSmallIcon = icon;
		return this;
	}

	public NotificationBuilder setNotiIcon(int icon) {
		mNotiIcon = icon;
		return this;
	}

	public NotificationBuilder setNumber(int number) {
		mNumber = number;
		return this;
	}

	public NotificationBuilder setContentTitle(CharSequence title) {
		mContentTitle = title;
		return this;
	}

	public NotificationBuilder setContentText(CharSequence text) {
		mContentText = text;
		return this;
	}

	public NotificationBuilder setContentInfo(CharSequence info) {
		mContentInfo = info;
		return this;
	}

	public NotificationBuilder setLatestEventTitle(CharSequence title) {
		mLatestEventTitle = title;
		return this;
	}

	public NotificationBuilder setLatestEventText(CharSequence text) {
		mLatestEventText = text;
		return this;
	}

	public NotificationBuilder setUseDefaultNotification(boolean bool) {
		mUseDefaultNotification = bool;
		return this;
	}

	public NotificationBuilder setSubText(CharSequence text) {
		mSubText = text;
		return this;
	}

	public NotificationBuilder setProgress(int max, int progress,
			boolean indeterminate) {
		mProgressMax = max;
		mProgress = progress;
		mProgressIndeterminate = indeterminate;
		return this;
	}

	public NotificationBuilder setContentIntent(PendingIntent intent) {
		mContentIntent = intent;
		return this;
	}

	public NotificationBuilder setTicker(CharSequence tickerText) {
		mTickerText = tickerText;
		return this;
	}

	public NotificationBuilder setLargeIcon(Bitmap icon) {
		mLargeIcon = icon;
		return this;
	}

	public NotificationBuilder setOngoing(boolean ongoing) {
		setFlag(Notification.FLAG_ONGOING_EVENT, ongoing);
		mOnGoing = ongoing;
		return this;
	}

	public NotificationBuilder setAutoCancel(boolean autoCancel) {
		setFlag(Notification.FLAG_AUTO_CANCEL, autoCancel);
		mAutoCancel = autoCancel;
		return this;
	}

	private void setFlag(int mask, boolean value) {
		if (value) {
			mFlags |= mask;
		} else {
			mFlags &= ~mask;
		}
	}

	private RemoteViews applyStandardTemplate(int resId, boolean fitIn1U) {
		RemoteViews contentView = new RemoteViews(mContext.getPackageName(),
				resId);
		boolean showLine3 = false;
		boolean showLine2 = false;
		int smallIconImageViewId = R.id.icon;
		if (mLargeIcon != null) {
			contentView.setImageViewBitmap(R.id.icon, mLargeIcon);
			smallIconImageViewId = R.id.right_icon;
		}

		if (mSmallIcon != 0) {
			contentView.setImageViewResource(smallIconImageViewId, mSmallIcon);
			contentView.setViewVisibility(smallIconImageViewId, View.VISIBLE);
		} else {
			contentView.setViewVisibility(smallIconImageViewId, View.GONE);
		}
		if (mContentTitle != null) {
			contentView.setTextViewText(R.id.title, mContentTitle);
		}
		if (mContentText != null) {
			contentView.setTextViewText(R.id.textb, mContentText);
			contentView.setTextViewText(R.id.texts, mContentText);
			showLine3 = true;
		}
		if (mContentInfo != null) {
			contentView.setTextViewText(R.id.info, mContentInfo);
			contentView.setViewVisibility(R.id.info, View.VISIBLE);
			showLine3 = true;
		} else {
			contentView.setViewVisibility(R.id.info, View.GONE);
		}

		if (mSubText != null) {
			contentView.setTextViewText(R.id.textb, mSubText);
			contentView.setTextViewText(R.id.texts, mSubText);
			if (mContentText != null) {
				contentView.setTextViewText(R.id.text2, mContentText);
				contentView.setViewVisibility(R.id.text2, View.VISIBLE);
				showLine2 = true;
			} else {
				contentView.setViewVisibility(R.id.text2, View.GONE);
			}
		} else {
			contentView.setViewVisibility(R.id.text2, View.GONE);
			if (mProgressMax != 0 || mProgressIndeterminate) {
				contentView.setProgressBar(R.id.progress, mProgressMax,
						mProgress, mProgressIndeterminate);
				contentView.setViewVisibility(R.id.progress, View.VISIBLE);
				showLine2 = true;
			} else {
				contentView.setViewVisibility(R.id.progress, View.GONE);
			}
		}

		if (showLine2) {
			if (fitIn1U) {
				contentView.setViewVisibility(R.id.textb, View.GONE);
				contentView.setViewVisibility(R.id.texts, View.VISIBLE);

			}
			contentView.setViewVisibility(R.id.line0, View.GONE);
		}

		if (mWhen != 0 && mShowWhen) {
			contentView.setViewVisibility(R.id.time, View.VISIBLE);
			contentView.setLong(R.id.time, "setTime", mWhen);
		} else {
			contentView.setViewVisibility(R.id.time, View.GONE);
		}

		contentView.setViewVisibility(R.id.line3, showLine3 ? View.VISIBLE
				: View.GONE);
		return contentView;
	}

	private RemoteViews makeContentView() {

		return applyStandardTemplate(R.layout.notification_template_base, true);

	}
	
	public Notification build() {

		Notification n = null;

		if (Build.VERSION.SDK_INT > 10) {

			Notification.Builder nb = new Notification.Builder(mContext);
			nb.setOngoing(mOnGoing);
			nb.setAutoCancel(mAutoCancel);
			nb.setContentIntent(mContentIntent);
			nb.setContentTitle(mContentTitle);
			nb.setSmallIcon(mSmallIcon);
			nb.setLargeIcon(mLargeIcon);
			nb.setContentText(mContentText);
			nb.setContentInfo(mContentInfo);
			if (mTickerText != null) {
				nb.setTicker(mTickerText);
			}
			nb.setWhen(mWhen);
			nb.setProgress(mProgressMax, mProgress, mProgressIndeterminate);
			n = nb.getNotification();

		} else if (Build.VERSION.SDK_INT < 9) {
				n = new Notification();
				n.when = mWhen;
				n.icon = mNotiIcon;
				if (mTickerText != null) {
					n.tickerText = mTickerText;
				}
				if (mLatestEventTitle == null) {
					mLatestEventTitle = mContentTitle;
				}
				if (mLatestEventText == null) {
					mLatestEventText = mContentText;
				}
				n.setLatestEventInfo(mContext, mLatestEventTitle,
						mLatestEventText, mContentIntent);
				n.flags = mFlags;
				n.number = mNumber;
		}
		
		else {
			if (mUseDefaultNotification) {
				n = new Notification();
				n.when = mWhen;
				n.icon = mNotiIcon;
				if (mTickerText != null) {
					n.tickerText = mTickerText;
				}
				n.setLatestEventInfo(mContext, mLatestEventTitle,
						mLatestEventText, mContentIntent);
				n.flags = mFlags;
				n.number = mNumber;
			} else {
				n = new Notification();
				n.when = mWhen;
				n.icon = mNotiIcon;
				if (mTickerText != null) {
					n.tickerText = mTickerText;
				}
				n.contentView = makeContentView();
				n.contentIntent = mContentIntent;
				n.flags = mFlags;
				n.number = mNumber;
			}
		}

		return n;
	}

}
