package de.manuel_voegele.cafeteria.tue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class UpdateCafeteriaListService extends IntentService
{
	public static final String LOG_TAG = UpdateCafeteriaListService.class.getSimpleName();
	
	public UpdateCafeteriaListService()
	{
		super(UpdateCafeteriaListService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		SQLiteDatabase db = SQLiteDatabase.openDatabase(new File(getFilesDir(), "database.db").getPath(), null, SQLiteDatabase.OPEN_READWRITE);
		String htmlCode;
		try
		{
			URL url = new URL("http://www.my-stuwe.de/cms/80/1/1/art/WasgibtesheuteinderMensaSpeiseplaene.html");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			PostParameterWriter writer = new PostParameterWriter(new OutputStreamWriter(connection.getOutputStream()));
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.WEEK_OF_YEAR, 1);
			writer.putParameter("selWeek", new SimpleDateFormat("y-w").format(calendar.getTime()));
			writer.close();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder pageSource = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null)
			{
				pageSource.append(line);
			}
			reader.close();
			htmlCode = pageSource.toString();
		}
		catch (MalformedURLException e)
		{
			intent = new Intent();
			intent.setAction(MainActivity.SHOW_ERROR_MESSAGE_ACTION);
			intent.putExtra("message", Integer.valueOf(R.string.error_unexpected));
			intent.putExtra("exception", e);
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			Log.wtf(LOG_TAG, e);
			db.close();
			return;
		}
		catch (IOException e)
		{
			intent = new Intent();
			intent.setAction(MainActivity.SHOW_ERROR_MESSAGE_ACTION);
			intent.putExtra("message", Integer.valueOf(R.string.error_data_fetch));
			intent.putExtra("exception", e);
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			Log.e(LOG_TAG, "Fetching data failed", e);
			db.close();
			return;
		}
		Pattern pattern = Pattern.compile("<select name=\"ORT_ID\".*?</select>");
		Matcher matcher = pattern.matcher(htmlCode);
		if (!matcher.find())
		{
			intent = new Intent();
			intent.setAction(MainActivity.SHOW_ERROR_MESSAGE_ACTION);
			intent.putExtra("message", Integer.valueOf(R.string.error_page_changed));
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			Log.e(LOG_TAG, "Cannot read data. Has the website changed?");
			db.close();
			return;
		}
		String select = matcher.group();
		// TODO Use pattern to extract information istead substring-methods
		pattern = Pattern.compile("<option.*?</option>");
		matcher = pattern.matcher(select);
		db.delete("cafeterias", null, null);
		while (matcher.find())
		{
			String option = matcher.group();
			option = StringUtils.substringAfter(option, "value='");
			if (option.isEmpty())
				continue;
			String id = StringUtils.substringBefore(option, "'");
			option = StringUtils.substringAfter(option, ">");
			String name = StringUtils.substringBefore(option, "<");
			ContentValues values = new ContentValues();
			values.put("id", Integer.valueOf(id));
			values.put("name", name);
			db.insert("cafeterias", null, values);
		}
		intent = new Intent();
		intent.setAction(MainActivity.SHOW_CAFETERIA_LIST_ACTION);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		db.close();
	}
}
