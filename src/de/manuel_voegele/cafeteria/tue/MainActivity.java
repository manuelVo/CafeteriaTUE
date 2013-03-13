package de.manuel_voegele.cafeteria.tue;

import java.io.File;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

/**
 * The activity showing the menu
 * 
 * @author Manuel Vögele
 */
public class MainActivity extends Activity
{
	/** Shows prompts the user to select a cafeteria */
	public static final String SHOW_CAFETERIA_LIST_ACTION = "ShowCafeteriaListAction";
	
	/**
	 * Shows an error message. The message should be defined in the extra
	 * 'message'. The exception (if available) should be defined in the extra
	 * 'exception'.
	 */
	public static final String SHOW_ERROR_MESSAGE_ACTION = "ShowErrorMessageAction";

	/**
	 * Switches to another cafeteria. The id of the cafeteria to show should be
	 * defined in the extra 'id'.
	 */
	public static final String SWITCH_CAFETERIA_ACTION = "SwitchCafeteriaAction";

	/**
	 * Refreshes the menu screen. If the extra hideProgress is set to
	 * <code>false</code> the progress bar will not be hidden.
	 */
	public static final String REFRESH_MENU_SCREEN_ACTION = "RefreshMenuScreenAction";

	/** The key of the preference for the cafeteria id */
	public static final String SETTING_CAFETERIA_ID = "cafeteriaId";

	/** A instance of the SQLiteDatabase */
	private SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SHOW_CAFETERIA_LIST_ACTION);
		intentFilter.addAction(SHOW_ERROR_MESSAGE_ACTION);
		intentFilter.addAction(SWITCH_CAFETERIA_ACTION);
		intentFilter.addAction(REFRESH_MENU_SCREEN_ACTION);
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
		setContentView(R.layout.main);

		File file = new File(getFilesDir(), "database.db");
		if (!file.exists())
		{
			db = SQLiteDatabase.openOrCreateDatabase(file, null);
			db.execSQL("CREATE TABLE cafeterias (id INT PRIMARY KEY, name TEXT);");
			db.execSQL("CREATE TABLE menus (cafeteriaid INT, type TEXT, menu TEXT, studentprice REAL, normalprice REAL, pupilprice REAL, day INT);");
			
			db.execSQL("INSERT INTO cafeterias (id, name) VALUES (621, 'Mensa Morgenstelle');");
			db.execSQL("INSERT INTO cafeterias (id, name) VALUES (631, 'Mensa Reutlingen');");
		}
		else
		{
			db = SQLiteDatabase.openDatabase(new File(getFilesDir(), "database.db").getPath(), null, SQLiteDatabase.OPEN_READWRITE);
		}
		int cafeteriaId = getPreferences(MODE_PRIVATE).getInt(SETTING_CAFETERIA_ID, -1);
		if (cafeteriaId == -1)
		{
			Intent intent = new Intent();
			intent.setAction(SHOW_CAFETERIA_LIST_ACTION);
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		}
		else
		{
			switchCafeteria(cafeteriaId);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
		db.close();
	}

	/**
	 * Called when the cafeteria list should be shown
	 */
	public void onShowCafeteriaListAction()
	{
		new CafeteriaSelection(this, db).show();
	}

	/**
	 * Switches the menu view to the specified cafeteria
	 * 
	 * @param intent
	 *           the intent used for this call
	 */
	public void onSwitchCafeteria(Intent intent)
	{
		int id = intent.getIntExtra("id", -1);
		getPreferences(MODE_PRIVATE).edit().putInt(SETTING_CAFETERIA_ID, id).apply();
		switchCafeteria(id);
	}

	/**
	 * Switches the menu view to the specified cafeteria
	 * 
	 * @param cafeteriaId
	 *           the id of the cafeteria
	 */
	public void switchCafeteria(int cafeteriaId)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Cursor cursor = db.rawQuery("SELECT * FROM menus WHERE cafeteriaid = ? AND day = ? LIMIT 0,1;", new String[] { String.valueOf(cafeteriaId), String.valueOf(calendar.getTimeInMillis()) });
		if (cursor.getCount() == 0)
		{
			Intent intent = new Intent(this, UpdateMenusService.class);
			intent.putExtra("cafeteriaid", cafeteriaId);
			startService(intent);
			findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
		}
		else
		{
			ViewPager pager = (ViewPager) findViewById(R.id.pager);
			DayAdapter adapter = new DayAdapter(this, cafeteriaId);
			pager.setAdapter(adapter);
			pager.setCurrentItem(adapter.getPageNumberForDay(calendar.getTimeInMillis()));
		}
		cursor.close();
	}

	/**
	 * Refreshes the menu screen
	 * 
	 * @param intent
	 *           the intent used for this call
	 */
	public void onRefreshMenuScreen(Intent intent)
	{
		if (intent.getBooleanExtra("hideProgress", true))
			findViewById(R.id.progressBar).setVisibility(View.GONE);
		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new DayAdapter(this, getPreferences(MODE_PRIVATE).getInt(SETTING_CAFETERIA_ID, -1)));
	}

	/**
	 * Shows an error message
	 * 
	 * @param intent
	 *           the intent used for this call
	 */
	public void onShowErrorMessage(Intent intent)
	{
		findViewById(R.id.progressBar).setVisibility(View.GONE);
		int errorMessage = intent.getIntExtra("message", R.string.unset);
		TextView text = new TextView(this);
		text.setText(errorMessage);
		new AlertDialog.Builder(this).setTitle(R.string.error).setView(text).setNeutralButton(R.string.ok, null).show();
	}

	/**
	 * The BroadcastReceiver for this Activity
	 */
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action.equals(REFRESH_MENU_SCREEN_ACTION))
				onRefreshMenuScreen(intent);
			else if (action.equals(SHOW_ERROR_MESSAGE_ACTION))
				onShowErrorMessage(intent);
			else if (action.equals(SWITCH_CAFETERIA_ACTION))
				onSwitchCafeteria(intent);
			else if (action.equals(SHOW_CAFETERIA_LIST_ACTION))
				onShowCafeteriaListAction();
		}
	};
}
