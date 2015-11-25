package com.withwiz.beach.network.http.client;

import com.withwiz.beach.network.http.message.*;

/**
 * HTTP processor interface.<BR/>
 * Created by uni4love on 2010. 5. 4..
 */
public interface IHttpProcessor<REQ extends IHttpMessage, RES extends IHttpMessage>
{
	/**
	 * HTTP Request message
	 *
	 * @param requestMessage
	 *            request message
	 * @return responeMessage
	 */
	RES request(REQ requestMessage);
}
