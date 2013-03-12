package de.manuel_voegele.cafeteria.tue;

public class StringUtils
{
	public static String substringBefore(String haystack, String needle)
	{
		int pos = haystack.indexOf(needle);
		if (pos == -1)
			return haystack;
		return haystack.substring(0, pos);
	}

	public static String substringAfter(String haystack, String needle)
	{
		int pos = haystack.indexOf(needle);
		if (pos == -1)
			return "";
		return haystack.substring(pos + needle.length());
	}
}
