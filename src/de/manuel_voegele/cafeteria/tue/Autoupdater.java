package de.manuel_voegele.cafeteria.tue;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * This class is responsible for updating the menu every day at 4 am.
 * 
 * @author Manuel Vögele
 * 
 */
public class Autoupdater extends BroadcastReceiver {

	/** {@link Intent} to update the menu automatically */
	public final static String ACTION_UPDATE = "UPDATE";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null)
			return;			
		SharedPreferences sp = context.getSharedPreferences(MainActivity.GENERAL_PREFERENCES_NAME, MainActivity.MODE_PRIVATE);
		String action = intent.getAction();
		if(Intent.ACTION_BOOT_COMPLETED.equals(action))
		{
			nextUpdate(context);
		}else if(ACTION_UPDATE.equals(action))
		{
			refreshMenus(context, sp.getInt(MainActivity.SETTING_CAFETERIA_ID, -1));
		}
	}
	
	/**
	 * Refreshes the menus
	 * 
	 * @param cafeteriaId
	 *           the id of the cafeteria to refresh the menus for
	 * @param context
	 *           the context
	 */
	public static void refreshMenus(Context context, int cafeteriaId)
	{
		Intent intent = new Intent(context, UpdateMenusService.class);
		intent.putExtra("cafeteriaid", cafeteriaId);
		context.startService(intent);
	}
	
	/**
	 * Set an alarm for the next update. This function is thread save
	 * (synchronized)
	 * 
	 * @param context
	 *           the context
	 */
	public static synchronized void nextUpdate(Context context)
	{
		// The Intent, witch send the signal for the update
		Intent i_alarm = new Intent();
		
		//Set the time for the next update
		Calendar updateTime = Calendar.getInstance();
		updateTime.add(Calendar.HOUR_OF_DAY, 20); //Update at 4 am. If the current time is before 4 o'clock the alarm will set today, else it will be called tomorrow
		updateTime.set(Calendar.HOUR_OF_DAY, 4);
		updateTime.set(Calendar.MINUTE, 0);
		updateTime.set(Calendar.SECOND, 0);
		updateTime.set(Calendar.MILLISECOND, 0);
		
		//Create the update period intent
		i_alarm.setComponent(new ComponentName("de.manuel_voegele.cafeteria.tue", Autoupdater.class.getName()));
		i_alarm.setAction(ACTION_UPDATE);

		//Set the AlarmManager to call the Intent at the specified time
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i_alarm, 0);
		((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
				.cancel(pi); //remove old Intent (needed to set a new)
		((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).set(
				AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), pi);
	}

}
