package de.manuel_voegele.cafeteria.tue;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CafeteriaListAdapter extends BaseAdapter implements OnClickListener
{
	private OnCafeteriaSelectedListener listener;

	private int[] cafeteriaIds;

	private String[] cafeteriaNames;

	private Context context;

	public CafeteriaListAdapter(Context context, OnCafeteriaSelectedListener listener)
	{
		SQLiteDatabase db = SQLiteDatabase.openDatabase(new File(context.getFilesDir(), "database.db").getPath(), null, SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery("SELECT id, name FROM cafeterias ORDER BY name;", null);
		cafeteriaIds = new int[cursor.getCount()];
		cafeteriaNames = new String[cursor.getCount()];
		cursor.moveToFirst();
		for (int i = 0;!cursor.isAfterLast();i++)
		{
			cafeteriaIds[i] = cursor.getInt(0);
			cafeteriaNames[i] = cursor.getString(1);
		}
		cursor.close();
		db.close();
		this.context = context;
		this.listener = listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		TextView textView = new TextView(context);
		textView.setText(cafeteriaNames[position]);
		textView.setOnClickListener(this);
		return null;
	}

	@Override
	public int getCount()
	{
		return cafeteriaIds.length;
	}

	@Override
	public Object getItem(int position)
	{
		return cafeteriaNames[position];
	}

	@Override
	public long getItemId(int position)
	{
		return cafeteriaIds[position];
	}

	@Override
	public void onClick(View v)
	{

	}

	public interface OnCafeteriaSelectedListener
	{
		public void onCafeteriaSelected(int cafeteriaId);
	}
}
