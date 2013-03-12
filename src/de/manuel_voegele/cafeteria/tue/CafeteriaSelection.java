package de.manuel_voegele.cafeteria.tue;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class CafeteriaSelection extends AlertDialog implements OnItemClickListener
{
	private Cursor cursor;

	private SQLiteDatabase db;

	public CafeteriaSelection(Context context, SQLiteDatabase db)
	{
		super(context);
		this.db = db;
		Cursor cursor = db.rawQuery("SELECT id AS _id, name FROM cafeterias ORDER BY name;", null);
		ListView listView = new ListView(context);
		listView.setAdapter(new SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, cursor, new String[] { "name" }, new int[] { android.R.id.text1 }, 0));
		listView.setOnItemClickListener(this);
		setView(listView);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		Intent intent = new Intent();
		intent.setAction(MainActivity.SWITCH_CAFETERIA_ACTION);
		intent.putExtra("id", (int) id);
		LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
		dismiss();
	}

}
