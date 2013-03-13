package de.manuel_voegele.cafeteria.tue;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.view.PagerAdapter;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class DayAdapter extends PagerAdapter
{
	private long[] days;
	
	private Map<Long, View> views = new HashMap<Long, View>();

	private Activity activity;

	private int cafeteriaId;

	public DayAdapter(Activity activity, int cafeteriaId)
	{
		this.activity = activity;
		this.cafeteriaId = cafeteriaId;
		SQLiteDatabase db = SQLiteDatabase.openDatabase(new File(activity.getFilesDir(), "database.db").getPath(), null, SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery("SELECT DISTINCT day FROM menus WHERE cafeteriaid = ?;", new String[] { String.valueOf(cafeteriaId) });
		days = new long[cursor.getCount()];
		for (int i = 0;cursor.move(1);i++)
		{
			days[i] = cursor.getLong(0);
		}
		cursor.close();
		db.close();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		long day = days[position];
		ListView listView = new ListView(activity);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(day);
		listView.setAdapter(new MenuListAdapter(activity, cafeteriaId, calendar));
		container.addView(listView);
		Long key = Long.valueOf(day);
		views.put(key, listView);
		return key;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		View view = views.get(object);
		views.remove(object);
		container.removeView(view);
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(days[position]);
		return DateFormat.getDateFormat(activity).format(calendar.getTime());
	}

	@Override
	public int getCount()
	{
		return days.length;
	}

	@Override
	public boolean isViewFromObject(View view, Object object)
	{
		View result = views.get(object);
		if (result == null)
			return false;
		if (!result.equals(view))
			return false;
		return true;
	}

	public int getPageNumberForDay(long day)
	{
		for (int i = 0;i < days.length;i++)
		{
			if (days[i] == day)
				return i;
		}
		return -1;
	}
}
