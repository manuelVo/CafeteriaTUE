package de.manuel_voegele.cafeteria.tue;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * The activity showing the settings
 * 
 * @author Manuel Vögele
 */
public class SettingsActivity extends Activity
{
	/** The key of the autoupdate setting */
	public static final String SETTING_AUTOUPDATE = "autoupdate";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	}

	public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);
		}

		@Override
		public void onResume()
		{
			super.onResume();
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause()
		{
			super.onPause();
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
		{
			if (SETTING_AUTOUPDATE.equals(key))
			{
				if (sharedPreferences.getBoolean(SETTING_AUTOUPDATE, true))
					Autoupdater.scheduleUpdate(getActivity());
				else
					Autoupdater.cancelScheduledUpdate(getActivity());
			}
		}
	}
}
