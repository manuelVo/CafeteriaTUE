package de.manuel_voegele.cafeteria.tue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * A service updating the menu for a specified cafeteria
 * 
 * The id of the
 * 
 * @author Manuel Vögele
 */
public class UpdateMenusService extends IntentService
{
	/**
	 * The log tag
	 */
	public static final String LOG_TAG = UpdateMenusService.class.getSimpleName();

	/**
	 * Initializes a new {@link UpdateMenusService}
	 */
	public UpdateMenusService()
	{
		super(UpdateMenusService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		int cafeteriaId = intent.getIntExtra("cafeteriaid", -1);
		if (cafeteriaId == -1)
		{
			intent = new Intent();
			intent.setAction(MainActivity.SHOW_ERROR_MESSAGE_ACTION);
			intent.putExtra("message", R.string.error_unexpected);
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			Log.wtf(LOG_TAG, "The cafeteriaid may not be -1 (unset)");
			return;
		}
		SQLiteDatabase db = SQLiteDatabase.openDatabase(new File(getFilesDir(), "database.db").getPath(), null, SQLiteDatabase.OPEN_READWRITE);
		Calendar calendar = Calendar.getInstance();
		try
		{
			for (int i = 0;true;i++)
			{
				String htmlCode = fetchMenuPage(cafeteriaId, calendar.getTime());
				if (!parsePage(htmlCode, db) && i != 0)
					break;
				calendar.add(Calendar.WEEK_OF_YEAR, 1);
			}
		}
		catch (MalformedURLException e)
		{
			intent = new Intent();
			intent.setAction(MainActivity.SHOW_ERROR_MESSAGE_ACTION);
			intent.putExtra("message", Integer.valueOf(R.string.error_unexpected));
			intent.putExtra("exception", e);
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			Log.wtf(LOG_TAG, "Fetching data failed", e);
			return;
		}
		catch (ParseException e)
		{
			intent = new Intent();
			intent.setAction(MainActivity.SHOW_ERROR_MESSAGE_ACTION);
			intent.putExtra("message", Integer.valueOf(R.string.error_unexpected));
			intent.putExtra("exception", e);
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			Log.wtf(LOG_TAG, "Fetching data failed", e);
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
			return;
		}
		finally
		{
			db.close();
		}
	}

	/**
	 * Loads the menu page for the for the specified week into a string
	 * 
	 * @param cafeteriaId
	 *           the id of the cafeteria
	 * @param week
	 *           a date in the week for which the data should be fetched
	 * @return the HTML code of the page
	 * @throws MalformedURLException
	 *            if fetching the menu page fails (this should never happen)
	 * @throws IOException
	 *            if fetching the menu page fails
	 */
	public static String fetchMenuPage(int cafeteriaId, Date week) throws MalformedURLException, IOException
	{
		URL url = new URL("http://www.my-stuwe.de/cms/80/1/1/art/WasgibtesheuteinderMensaSpeiseplaene.html");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		PostParameterWriter writer = new PostParameterWriter(new OutputStreamWriter(connection.getOutputStream()));
		//			writer.write("selWeek=2013-11&ORT_ID=631&selView=liste&aktion=changeWeek&vbLoc=&lang=1&client=");
		writer.putParameter("ORT_ID", String.valueOf(cafeteriaId));
		writer.putParameter("selWeek", new SimpleDateFormat("y-w").format(week));
		writer.putParameter("selView", "liste");
		writer.putParameter("lang", "1");
		writer.putParameter("aktion", "changeWeek");
		writer.putParameter("vbLoc", "");
		writer.putParameter("client", "");
		writer.close();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder pageSource = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null)
		{
			pageSource.append(line);
		}
		reader.close();
		return pageSource.toString();
	}

	/**
	 * Parses the HTML page and writes the new menus into the database. The old
	 * menus of the refreshed days will be deleted.
	 * 
	 * @param htmlCode
	 *           the HTML code of the menu page
	 * @param db
	 *           the database to store the menu in
	 * @return <code>true</code> if any menus were parsed
	 * @throws ParseException
	 *            if parsing the date fails - most likely the site has changed
	 */
	public static boolean parsePage(String htmlCode, SQLiteDatabase db) throws ParseException
	{
		htmlCode = StringUtils.substringAfter(htmlCode, "<div class=\"\">");
		htmlCode = StringUtils.substringBefore(htmlCode, "<table class");
		htmlCode = htmlCode.replace("\n", " ");
		SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.y");
		Pattern dayPattern = Pattern.compile("<div.*?>.*?, (.*?)</div>.*?<table.*?>(.*?)</table>");
		Matcher dayMatcher = dayPattern.matcher(htmlCode);

		Pattern menuRowPattern = Pattern.compile("<tr.*?</tr>");
		//		Pattern menuTypePattern = Pattern.compile("<td.*?>(.*?)</td>");
		//		Pattern menuMenuPattern = Pattern.compile("<td.*?>\\s*(.*?)\\s*&nbsp;");
		//		Pattern priceNormalPattern = Pattern.compile("<td:*?G.ste: (.*?) ");
		//		Pattern pricePupilPattern = Pattern.compile("Schüler:(.*?) ");
		//		Pattern priceStudentPattern = Pattern.compile("&nbsp;\\s*(.*?) ");
		// The fifth group is for parsing, not for reading data
		// TODO Fix "Gäste" (its currently G.ste) and "Schüler" (currently Sch.ler) when encoding is fixed
		Pattern menuPattern = Pattern.compile("<td.*?>(.*?)</td>.*?<td.*?>\\s*(.*?)\\s*&nbsp;.*?-->.*?<td.*?G.ste: (.*?) .*?Sch.ler:(.*?) .*?>(\\s|&nbsp;)*(.*?) ");
		if (!dayMatcher.find())
			return false;
		do
		{
			Date day = dateFormat.parse(dayMatcher.group(1));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(day);
			Long timestamp = Long.valueOf(calendar.getTimeInMillis());
			//			Matcher menuMatcher = menuPattern.matcher(dayMatcher.group(2));
			Matcher menuRowMatcher = menuRowPattern.matcher(dayMatcher.group(2));
			// FIXME Remove next line - testing only
			menuRowMatcher.find();
			while (menuRowMatcher.find())
			{
				String menuRow = menuRowMatcher.group();
				Matcher menuMatcher = menuPattern.matcher(menuRow);
				if (!menuMatcher.find())
				{
					// FIXME Do something more intelligent
					throw new RuntimeException("Of all the worst things that could happen, this is THE WORST POSSIBLE THING");
				}
				String menuType = menuMatcher.group(1);
				String menuMenu = menuMatcher.group(2);
				String priceNormal = menuMatcher.group(3);
				String pricePupil = menuMatcher.group(4);
				// Group 5 is used for parsing purposes
				String priceStudent = menuMatcher.group(6);
				pricePupil = pricePupil.replace(',', '.');
				priceNormal = priceNormal.replace(',', '.');
				priceStudent = priceStudent.replace(',', '.');
				menuMenu = menuMenu.replace("<br />", ", ");
				ContentValues values = new ContentValues();
				values.put("type", menuType);
				values.put("menu", menuMenu);
				values.put("normalprice", Double.valueOf(priceNormal));
				values.put("pupilprice", Double.valueOf(pricePupil));
				values.put("studentprice", Double.valueOf(priceStudent));
				values.put("day", timestamp);
				db.insert("menus", null, values);
			}
		} while (dayMatcher.find());
		return true;
	}
}
