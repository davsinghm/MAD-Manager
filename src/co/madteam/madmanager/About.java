package co.madteam.madmanager;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

public class About extends SherlockPreferenceActivity {

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			super.onBackPressed();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setTheme(Theme.getTheme(this));

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.pref_screen);

		EasyTracker.getInstance().activityStart(this);

		PreferenceScreen PrefScreen = getPreferenceManager()
				.createPreferenceScreen(this);
				
		Preference Title = new Preference(this);
		Title.setTitle(getText(R.string.app_name) + " v" + getText(R.string.version));
		Title.setSummary(getText(R.string.copyright));
		Title.setEnabled(true);
		Title.setLayoutResource(R.layout.pref);
		PrefScreen.addPreference(Title);

		PreferenceCategory PrefCat = new PreferenceCategory(this);
		PrefCat.setTitle(getText(R.string.AUTHOR));
		PrefScreen.addPreference(PrefCat);

		PrefLinkify pref = new PrefLinkify(this);
		pref.setTitle(getText(R.string.written_by) + " DSM_");
		pref.setSummary(getText(R.string.about_author));
		pref.setLinkify(true);
		pref.setEnabled(true);
		pref.setLayoutResource(R.layout.pref);
		PrefCat.addPreference(pref);

		PreferenceCategory PrefCat1 = new PreferenceCategory(this);
		PrefCat1.setTitle(getText(R.string.CREDITS));
		PrefScreen.addPreference(PrefCat1);

		Preference pref1 = new Preference(this);
		pref1.setTitle(getText(R.string.motafoca));
		pref1.setSummary(getText(R.string.motafoca_sum));
		pref1.setEnabled(true);
		pref1.setLayoutResource(R.layout.pref);
		PrefCat1.addPreference(pref1);

		Preference pref2 = new Preference(this);
		pref2.setTitle(getText(R.string.nend));
		pref2.setSummary(getText(R.string.nend_sum));
		pref2.setEnabled(true);
		pref2.setLayoutResource(R.layout.pref);
		PrefCat1.addPreference(pref2);
		
		Preference pref3 = new Preference(this);
		pref3.setTitle(getText(R.string.psyke83));
		pref3.setSummary(getText(R.string.psyke83_sum));
		pref3.setEnabled(true);
		pref3.setLayoutResource(R.layout.pref);
		PrefCat1.addPreference(pref3);
		
		PreferenceCategory Translators = new PreferenceCategory(this);
		Translators.setTitle(getText(R.string.TRANSLATORS));
		PrefScreen.addPreference(Translators);

		Preference Tpref1 = new Preference(this);
		Tpref1.setTitle("EsromG5");
		Tpref1.setSummary("Portuguese (Brazil)");
		Tpref1.setEnabled(true);
		Tpref1.setLayoutResource(R.layout.pref);
		Translators.addPreference(Tpref1);
		
		Preference Tpref2 = new Preference(this);
		Tpref2.setTitle("Gspin");
		Tpref2.setSummary("Italian");
		Tpref2.setEnabled(true);
		Tpref2.setLayoutResource(R.layout.pref);
		Translators.addPreference(Tpref2);
		
		Preference Tpref3 = new Preference(this);
		Tpref3.setTitle("cart00nero");
		Tpref3.setSummary("Spanish");
		Tpref3.setEnabled(true);
		Tpref3.setLayoutResource(R.layout.pref);
		Translators.addPreference(Tpref3);
		
		
		setPreferenceScreen(PrefScreen);

	}
	
	  @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance().activityStop(this);
	  }

}