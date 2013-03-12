package de.manuel_voegele.cafeteria.tue;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * A factory for creating views
 *
 * @author Manuel Vögele
 */
public class ViewFactory
{
	/**
	 * Creates a new view from the specified resource
	 * 
	 * @param resource the resource
	 * @param activity the activity for which the view is created
	 * @return the new view
	 */
	public static FrameLayout createView(int resource, Activity activity)
	{
		FrameLayout root = new FrameLayout(activity);
		createView(resource, root, activity);
		return root;
	}
	
	/**
	 * Creates a new view from the specified resource and adds it to element parent
	 * 
	 * @param resource the resource
	 * @param parent the parent element
	 * @param activity the activity for which the view is created
	 * @return the new view
	 */
	public static <T extends View> T createView(int resource, ViewGroup parent, Activity activity)
	{
		return createView(resource, parent, activity.getLayoutInflater());
	}
	
	/**
	 * Creates a new view from the specified resource and adds it to element parent
	 * 
	 * @param resource the resource
	 * @param parent the parent element
	 * @param inflater the layout inflater used to create new views
	 * @return the new view
	 */
	@SuppressWarnings("unchecked")
	public static <T extends View> T createView(int resource, ViewGroup parent, LayoutInflater inflater)
	{
		parent = (ViewGroup) inflater.inflate(resource, parent);
		return (T) parent.getChildAt(parent.getChildCount() - 1);
	}
}
