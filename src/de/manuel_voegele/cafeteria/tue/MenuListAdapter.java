package de.manuel_voegele.cafeteria.tue;

import java.io.File;
import java.util.Calendar;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MenuListAdapter extends BaseAdapter
{
	private int cafeteriaId;

	private String[] types;

	private String[] menus;

	private String[] prices;

	private Activity activity;

	public MenuListAdapter(Activity activity, int cafeteriaId, Calendar day)
	{
		day.set(Calendar.HOUR_OF_DAY, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);
		this.activity = activity;
		this.cafeteriaId = cafeteriaId;
		SQLiteDatabase db = SQLiteDatabase.openDatabase(new File(activity.getFilesDir(), "database.db").getPath(), null, SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery("SELECT type, menu, studentprice, normalprice, pupilprice FROM menus WHERE day = ?;", new String[] { String.valueOf(day.getTimeInMillis()) });
		types = new String[cursor.getCount()];
		menus = new String[cursor.getCount()];
		prices = new String[cursor.getCount()];
		cursor.moveToFirst();
		for (int i = 0;!cursor.isAfterLast();i++)
		{
			types[i] = cursor.getString(0);
			menus[i] = cursor.getString(1);
			prices[i] = cursor.getDouble(2) + " / " + cursor.getDouble(3) + " / " + cursor.getDouble(4);
			cursor.move(1);
		}
		cursor.close();
		db.close();
	}

	@Override
	public int getCount()
	{
		return types.length;
	}

	@Override
	public Object getItem(int position)
	{
		return menus[position];
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = ViewFactory.createView(R.layout.menu_row, activity);
		TextView type = (TextView) row.findViewById(R.id.title);
		type.setText(types[position]);
		TextView menu = (TextView) row.findViewById(R.id.menu);
		menu.setText(menus[position]);
		TextView price = (TextView) row.findViewById(R.id.price);
		price.setText(prices[position]);
		return row;
	}
}
