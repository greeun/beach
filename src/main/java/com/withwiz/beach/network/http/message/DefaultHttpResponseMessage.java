package com.withwiz.beach.network.http.message;

/**
 * HTTP response message.<BR/>
 * Created by uni4love on 2010. 5. 8..
 */
public class DefaultHttpResponseMessage extends DefaultHttpMessage
{
	/**
	 * default name
	 */
	protected static final String 		NAME_DEFAULT = "HTTP_RES_MESSAGE";

	@Override
	public byte[] getBodyByteArray()
	{
		if (bodyInputStream != null)
		{
			return super.getBodyByteArray();
		}
		else
		{
			return "No Content".getBytes();
		}
	}

	@Override
	public String toString()
	{
		return new StringBuffer(super.toString()).toString();
	}

	@Override
	public String getName()
	{
		return name == null ? NAME_DEFAULT : name;
	}
}
