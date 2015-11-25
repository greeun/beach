package com.withwiz.beach.network.http.message;

/**
 * HTTP Request message interface.<BR/>
 * Created by uni4love on 2012.07.20..
 */
public interface IHttpRequestMessage extends IHttpMessage
{
	/**
	 * return attachment file path
	 *
	 * @return file path
	 */
	String getAttachFilePath();

	/**
	 * return body name
	 * 
	 * @return name
	 */
	String getBodyName();

	/**
	 * return body type
	 * 
	 * @return type
	 */
	int getBodyType();



}
