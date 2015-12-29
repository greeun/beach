package com.withwiz.beach.network.http.message;

/**
 * HTTP Request message interface.<BR/>
 * Created by uni4love on 2012.07.20..
 */
public interface IHttpRequestMessage extends IHttpMessage
{
	/**
	 * status: undefined
	 */
	int UNDEFINED = -1;

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

	/**
	 * return connection timeout.<BR/>
	 *
	 * @return connection timeout(milliseconds)
	 */
	int getConnectionTimeout();

	/**
	 * return socket timeout.<BR/>
	 *
	 * @return socket timeout(milliseconds)
	 */
	int getSocketTimeout();


	/**
	 * return network buffer size.<BR/>
	 *
	 * @return network buffer size(bytes)
	 */
	int getNetworkBufferSize();

}
