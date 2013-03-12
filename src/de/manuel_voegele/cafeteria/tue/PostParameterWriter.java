package de.manuel_voegele.cafeteria.tue;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

public class PostParameterWriter implements Closeable, Flushable
{
	private Writer writer;

	private boolean firstParameter = true;

	public PostParameterWriter(Writer writer)
	{
		this.writer = writer;
	}

	public void putParameter(String name, String value) throws IOException
	{
		if (firstParameter)
			firstParameter = false;
		else
			writer.append('&');
		writer.write(name);
		writer.append('=');
		writer.write(value);
	}

	@Override
	public void close() throws IOException
	{
		writer.close();
	}

	@Override
	public void flush() throws IOException
	{
		writer.flush();
	}

}
