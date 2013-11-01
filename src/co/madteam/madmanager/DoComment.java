package co.madteam.madmanager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import co.madteam.madmanager.utilities.MadUtils;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

public class DoComment {

	Context mContext;
	int mAccountSelected;
	int mID;
	NewDownload.LoadComments mLoadComments;

	DoComment(Context c, int id,  NewDownload.LoadComments asyncTask) {
		mContext = c;
		mID = id;
		mLoadComments = asyncTask;
		readyToComment();
	}

	public void selectAccount() {

		mAccountSelected = -1;

		AccountManager manager = AccountManager.get(mContext);
		Account[] accounts = manager.getAccountsByType("com.google");
		ArrayList<String> arrayList = new ArrayList<String>();

		for (Account account : accounts) {
			arrayList.add(account.name);
		}

		if (arrayList.size() == 0) {
			AlertDialog.Builder noAcc = new AlertDialog.Builder(mContext);
			noAcc.setTitle(mContext.getText(R.string.no_google_account));
			noAcc.setMessage(mContext.getText(R.string.no_google_account_msg));
			noAcc.setNegativeButton(mContext.getText(R.string.ok), null);
			noAcc.show();
			return;
		}

		final String[] accountList = new String[arrayList.size()];
		arrayList.toArray(accountList);

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(mContext.getText(R.string.select_account));
		builder.setSingleChoiceItems(accountList, -1,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {
						mAccountSelected = arg1;
					}
				});
		builder.setPositiveButton(mContext.getText(R.string.ok),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences.Editor editor = PreferenceManager
								.getDefaultSharedPreferences(mContext).edit();
						editor.putString("google_account",
								accountList[mAccountSelected]).commit();

						enterNickname();
					}

				});
		builder.setNegativeButton(mContext.getText(R.string.cancel), null);
		builder.show();

	}

	public void enterNickname() {

		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		View view = new View(mContext);
		View layout = inflater.inflate(R.layout.dialog_username,
				(ViewGroup) view.findViewById(R.id.username));

		final EditText edittext = (EditText) layout
				.findViewById(R.id.usernameedit);
		edittext.selectAll();

		final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

		builder.setView(layout);

		builder.setTitle(mContext.getText(R.string.usernamehint));
		builder.setPositiveButton(mContext.getText(R.string.ok),
				new OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {

						String str = edittext.getText().toString();
						if (str.length() == 0) {
							Toast.makeText(mContext,
									mContext.getText(R.string.invalidname),
									Toast.LENGTH_LONG).show();
						} else if (str.length() > 0) {
							SharedPreferences.Editor editor = PreferenceManager
									.getDefaultSharedPreferences(mContext)
									.edit();
							editor.putString("username", str).commit();

							readyToComment();
						}
					}

				});
		builder.setNegativeButton(mContext.getText(R.string.cancel), null);
		builder.show();
	}

	public void readyToComment() {

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		String current_account = sharedPreferences.getString("google_account",
				"null");

		String username = sharedPreferences.getString("username", "null");

		if (MadUtils.isEmpty(current_account)) {
			selectAccount();
		} else if (MadUtils.isEmpty(username)) {
			enterNickname();
		} else {
			doComment(current_account, username);
		}

	}

	public void doComment(final String account, final String username) {
		
		if (mID == 0) {
			return;
		}

		View view = View.inflate(mContext, R.layout.dialog_comment, null);

		final EditText comment = (EditText) view.findViewById(R.id.comment);

		final RatingBar ratingBar = (RatingBar) view
				.findViewById(R.id.doRating);
		ratingBar.setMax(5);

		AlertDialog.Builder a = new AlertDialog.Builder(mContext)
				.setTitle(mContext.getText(R.string.comment_hint))
				.setView(view)
				.setPositiveButton(mContext.getText(R.string.submit),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

								String cmt = comment.getText().toString()
										.replace(" ", "+").replace("&", "and")
										.replace("\n", "+");
								String rating = "" + ratingBar.getRating();
								if (rating.contains(".")) {
									rating = rating.replace(".0", "");
								}
								if (rating.equals("0") || cmt.length() == 0) {
									Toast.makeText(
											mContext,
											mContext.getText(R.string.enter_valid_comment),
											Toast.LENGTH_LONG).show();
									return;
								}

								String postcomment =

								"http://www.2-si.net/_roms/?do=comment&gid="
										+ account +
										"&rate=" + rating + "&name=" + username
										+ "&comment=" + cmt + "&id=" + mID;

								new AsyncTask<String, String, String>() {

									ProgressDialog mProgressDialog;

									@Override
									protected void onPreExecute() {
										mProgressDialog = new ProgressDialog(
												mContext);
										mProgressDialog.setMessage(mContext
												.getText(R.string.processing));
										mProgressDialog
												.setProgressStyle(ProgressDialog.STYLE_SPINNER);
										mProgressDialog.setCancelable(false);
										mProgressDialog.show();
									}

									@Override
									protected String doInBackground(
											String... arg0) {

										try {

											URL readJSON = new URL(arg0[0]);
											URLConnection tc = readJSON
													.openConnection();
											tc.getContent();

										} catch (MalformedURLException e) {
											return "error";
										} catch (IOException e) {
											return "error";
										}
										return "success";
									}

									@Override
									protected void onPostExecute(String result) {
										mProgressDialog.cancel();

										if (result.equals("error")) {
											Toast.makeText(
													mContext,
													mContext.getText(R.string.unable_to_comment),
													Toast.LENGTH_LONG).show();
										} else {
											Toast.makeText(
													mContext,
													mContext.getText(R.string.comment_added_successfully),
													Toast.LENGTH_LONG).show();
										}
										mLoadComments.execute();

									}

								}.execute(postcomment);
							}
						})
				.setNegativeButton(mContext.getText(R.string.cancel), null);
		a.show();
	}

}