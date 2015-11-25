package com.withwiz.beach.network.http.message;

import java.net.MalformedURLException;

/**
 * HTTP request message.<BR/>
 * Created by uni4love on 2010. 5. 8..
 */
public class DefaultHttpRequestMessage extends DefaultHttpMessage implements IHttpRequestMessage
{
	/**
	 * default name
	 */
	protected static final String NAME_DEFAULT = "HTTP_REQ_MESSAGE";

	/**
	 * body name
	 */
	protected String bodyName;

	/**
	 * body type
	 */
	protected int bodyType = BODY_TYPE_UNDEFINED;

	/**
	 * attach file path
	 */
	protected String attachFilePath = null;

	/**
	 * constructor
	 */
	public DefaultHttpRequestMessage()
	{
	}

	/**
	 * constructor
	 *
	 * @param url
	 *            request url
	 */
	public DefaultHttpRequestMessage(String url) throws MalformedURLException
	{
		super(url);
	}

	/**
	 * constructor
	 * 
	 * @param name
	 *            name
	 * @param url
	 *            request url
	 * @throws MalformedURLException
	 */
	public DefaultHttpRequestMessage(String name, String url)
			throws MalformedURLException
	{
		super(name, url);
	}

	/**
	 * return attach file path
	 *
	 * @return file path
	 */
	@Override
	public String getAttachFilePath()
	{
		return attachFilePath;
	}

	@Override
	public String getBodyName()
	{
		return bodyName;
	}

	public void setBodyName(String bodyName)
	{
		this.bodyName = bodyName;
	}

	@Override
	public int getBodyType()
	{
		return bodyType;
	}

	public void setBodyType(int bodyType)
	{
		this.bodyType = bodyType;
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		if (bodyType != BODY_TYPE_UNDEFINED)
			sb.append("-bodyType: ").append(bodyType).append("\n");
		if (attachFilePath != null)
			sb.append("-attachFilePath: ").append(attachFilePath).append("\n");
		return sb.toString();
	}

}
