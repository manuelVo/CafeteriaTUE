package de.manuel_voegele.cafeteria.tue;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
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

public class MainActivity extends Activity implements OnCancelListener
{
	public static final String SHOW_CAFETERIA_LIST_ACTION = "ShowCafeteriaListAction";
	
	public static final String SHOW_ERROR_MESSAGE_ACTION = "ShowErrorMessageAction";

	public static final String SWITCH_CAFETERIA_ACTION = "SwitchCafeteriaAction";

	public static final String REFRESH_MENU_SCREEN_ACTION = "RefreshMenuScreenAction";

	AlertDialog dialog;

	SQLiteDatabase db;

	int cafeteriaId;

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
		File file = new File(getFilesDir(), "database.db");
		// TODO Remove next line
		file.delete();
		if (!file.exists())
		{
			db = SQLiteDatabase.openOrCreateDatabase(file, null);
			db.execSQL("CREATE TABLE cafeterias (id INT PRIMARY KEY, name TEXT);");
			db.execSQL("CREATE TABLE menus (cafeteriaid INT, type TEXT, menu TEXT, studentprice REAL, normalprice REAL, pupilprice REAL, day INT);");
			db.execSQL("CREATE TABLE settings (key TEXT, value INT);");
			
			db.execSQL("INSERT INTO cafeterias (id, name) VALUES (621, 'Mensa Morgenstelle');");
			db.execSQL("INSERT INTO cafeterias (id, name) VALUES (631, 'Mensa Reutlingen');");
			Intent intent = new Intent();
			intent.setAction(SHOW_CAFETERIA_LIST_ACTION);
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			
//			startService(new Intent(this, UpdateCafeteriaListService.class));
//			FrameLayout popupLoadingView = ViewFactory.createView(R.layout.popup_loading, this);
//			TextView text = (TextView) popupLoadingView.findViewById(R.id.text);
//			text.setText(R.string.downloading_cafeteria_list);
//			dialog = new AlertDialog.Builder(this).setView(popupLoadingView).setOnCancelListener(this).show();
		}
		else
		{
			db = SQLiteDatabase.openDatabase(new File(getFilesDir(), "database.db").getPath(), null, SQLiteDatabase.OPEN_READWRITE);
		}
		setContentView(R.layout.main);
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

	@Override
	public void onCancel(DialogInterface dialog)
	{
		File file = new File(getFilesDir(), "database.db");
		file.delete();
		finish();
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action.equals(REFRESH_MENU_SCREEN_ACTION))
				onRefreshMenuScreen();
			else if (action.equals(SHOW_ERROR_MESSAGE_ACTION))
				onShowErrorMessage(intent);
			else if (action.equals(SWITCH_CAFETERIA_ACTION))
				onSwitchCafeteria(intent);
			else if (action.equals(SHOW_CAFETERIA_LIST_ACTION))
				onShowCafeteriaListAction();
		}

		public void onShowCafeteriaListAction()
		{
//			dialog.dismiss();
//			dialog = null;
			new CafeteriaSelection(MainActivity.this, db).show();
		}

		public void onSwitchCafeteria(Intent intent)
		{
			int id = intent.getIntExtra("id", -1);
			cafeteriaId = id;
			Cursor cursor = db.rawQuery("SELECT * FROM menus WHERE cafeteriaid = ? LIMIT 0,1;", new String[] { String.valueOf(id) });
			if (cursor.getCount() == 0)
			{
				intent = new Intent(MainActivity.this, UpdateMenusService.class);
				intent.putExtra("cafeteriaid", id);
				startService(intent);
				findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
			}
			cursor.close();
			ViewPager pager = (ViewPager) findViewById(R.id.pager);
			pager.setAdapter(new DayAdapter(MainActivity.this, id));
		}

		public void onRefreshMenuScreen()
		{
			findViewById(R.id.progressBar).setVisibility(View.GONE);
			ViewPager pager = (ViewPager) findViewById(R.id.pager);
			pager.setAdapter(new DayAdapter(MainActivity.this, cafeteriaId));
		}

		public void onShowErrorMessage(Intent intent)
		{
			if (dialog != null)
			{
				dialog.dismiss();
				dialog = null;
			}
			findViewById(R.id.progressBar).setVisibility(View.GONE);
			int errorMessage = intent.getIntExtra("message", R.string.unset);
			TextView text = new TextView(MainActivity.this);
			text.setText(errorMessage);
			new AlertDialog.Builder(MainActivity.this).setTitle(R.string.error).setView(text).setNeutralButton(R.string.ok, null).show();
		}
	};
}
