package com.withwiz.beach.network.http.message;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.withwiz.plankton.io.ProxyInputStream;
import com.withwiz.plankton.network.INetworkStatus;

/**
 * HTTP common message<BR/>
 * Created by uni4love on 2010. 5. 8..
 */
public class DefaultHttpMessage extends HttpStatusExtended
		implements IHttpMessage, Cloneable
{
	/**
	 * http method: get
	 */
	public static final int		METHOD_GET		= 1;

	/**
	 * http method: post
	 */
	public static final int		METHOD_POST		= 2;

	/**
	 * http method: put
	 */
	public static final int		METHOD_PUT		= 3;

	/**
	 * http method: delete
	 */
	public static final int		METHOD_DELETE	= 4;

	/**
	 * protocol: http
	 */
	private static final String	PROTOCOL_HTTP	= "http";

	/**
	 * protocol: https
	 */
	private static final String PROTOCOL_HTTPS = "https";

	/**
	 * protocol divider
	 */
	private static final String PROTOCOL_DIVIDER = "://";

	/**
	 * body print length limit
	 */
	private static final int BODY_PRINT_LENGTH_LIMIT = 4096;

	/**
	 * body print message length limit
	 */
	private static final String MESSAGE_BODY_LENGTHS_BIGGER = "The body length is over than "
			+ BODY_PRINT_LENGTH_LIMIT + ".";

	/**
	 * code-strings repository
	 */
	private static Map<Integer, String> methodStrings = null;

	/**
	 * initializing status code strings.
	 */
	static
	{
		methodStrings = new HashMap<Integer, String>();
		Field[] fields = DefaultHttpMessage.class.getFields();
		for (Field field : fields)
		{
			try
			{
				if (field.getName().startsWith("METHOD_"))
				{
					methodStrings.put(field.getInt(DefaultHttpMessage.class),
							field.getName().replace("METHOD_", ""));
				}
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * name
	 */
	protected String				name				= null;

	/**
	 * URL
	 */
	protected URL					url					= null;

	/**
	 * http method
	 */
	protected int					method				= METHOD_GET;

	/**
	 * content-type
	 */
	protected String				contentType			= null;

	/**
	 * status code
	 */
	protected int					statusCode			= -1;

	/**
	 * header parameter list
	 */
	protected Map<String, String>	headers				= null;

	/**
	 * encoding
	 */
	protected String				textEncoding		= "UTF-8";

	/**
	 * body parameter list
	 */
	protected Map<String, String>	parameters			= null;

	/**
	 * "Content-Disposition" header parameter object
	 */
	protected ContentDisposition	contentDisposition	= null;

	/**
	 * body InputStream
	 */
	protected InputStream			bodyInputStream		= null;

	/**
	 * using HTTPS
	 */
	private boolean					isHttps				= false;

	/**
	 * Non trust
	 */
	private boolean isSslTrust = true;

	/**
	 * constructor
	 */
	public DefaultHttpMessage()
	{
		headers = new HashMap<String, String>();
		parameters = new HashMap<String, String>();
	}

	/**
	 * constructor
	 *
	 * @param url
	 *            url
	 */
	public DefaultHttpMessage(String url) throws MalformedURLException
	{
		this();
		setUrl(url);
	}

	/**
	 * constructor
	 *
	 * @param name
	 *            name
	 * @param url
	 *            url
	 */
	public DefaultHttpMessage(String name, String url)
			throws MalformedURLException
	{
		this();
		setName(name);
		setUrl(url);
	}

	/**
	 * return string text related with HTTP method code.
	 *
	 * @param code
	 *            method code
	 * @return method string
	 */
	public static String getMethodString(int code)
	{
		return methodStrings.get(code);
	}

	/**
	 * return HTTP method code Set instance.<BR/>
	 *
	 * @return Set
	 */
	public static Set getMethodSet()
	{
		return methodStrings.keySet();
	}

	/**
	 * return name.<BR/>
	 *
	 * @return name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * set this message name.<BR/>
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * add a header parameter.<BR/>
	 *
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	@Override
	public void addHeaderParameter(String key, String value)
	{
		headers.put(key, value);
	}

	/**
	 * return value related with key.<BR/>
	 *
	 * @param key
	 *            key
	 * @return value
	 */
	@Override
	public String getHeaderValue(String key)
	{
		return headers.get(key);
	}

	/**
	 * return key list array from header parameter list.<BR/>
	 *
	 * @return key String[]
	 */
	@Override
	public String[] getHeaderNames()
	{
		if (headers != null)
		{
			return headers.keySet().toArray(new String[headers.size()]);
		}
		return null;
	}

	/**
	 * return header parameter size.<BR/>
	 *
	 * @return size
	 */
	@Override
	public int getHeaderSize()
	{
		return headers.size();
	}

	/**
	 * add a parameter.<BR/>
	 *
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	@Override
	public void addParameter(String key, String value)
	{
		parameters.put(key, value);
	}

	/**
	 * return value related with key.<BR/>
	 *
	 * @param key
	 *            key
	 * @return value
	 */
	@Override
	public String getParameterValue(String key)
	{
		return parameters.get(key);
	}

	/**
	 * return key list array from parameter list.<BR/>
	 *
	 * @return key String[]
	 */
	@Override
	public String[] getParameterNames()
	{
		if (parameters != null)
		{
			return parameters.keySet().toArray(new String[parameters.size()]);
		}
		return null;
	}

	/**
	 * return parameter size.<BR/>
	 *
	 * @return size
	 */
	@Override
	public int getParameterSize()
	{
		return parameters.size();
	}

	/**
	 * return InputStream for body.<BR/>
	 *
	 * @return InputStream
	 */
	@Override
	public InputStream getBodyInputStream()
	{
		return bodyInputStream;
	}

	/**
	 * set InputStream for body.<BR/>
	 *
	 * @param inputStream
	 *            InputStream for body
	 */
	@Override
	public void setBodyInputStream(InputStream inputStream)
	{
		this.bodyInputStream = inputStream;
	}

	/**
	 * return body contents as byte[].<BR/>
	 *
	 * @return body content byte[]
	 */
	public byte[] getBodyByteArray()
	{
		ProxyInputStream pis = null;
		try
		{
			pis = new ProxyInputStream(this.bodyInputStream);
			if (this.bodyInputStream != null)
				this.bodyInputStream.close();

			this.bodyInputStream = pis.getNewInputStream();
			return pis.getData();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			try
			{
				if (pis != null)
				{
					pis.close();
					pis = null;
				}
			}
			catch (IOException e2)
			{
				e2.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * return use state https protocol
	 *
	 * @return sdtate
	 */
	@Override
	public boolean isHttps()
	{
		return isHttps;
	}

	/**
	 * set use or not https protocol
	 *
	 * @param https
	 *            use or not
	 */
	@Override
	public void setHttps(boolean https)
	{

		isHttps = https;
	}

	/**
	 * return using HTTP method.<BR/>
	 *
	 * @return HTTP method
	 */
	@Override
	public int getMethod()
	{

		return method;
	}

	/**
	 * set HTTP method.<BR/>
	 *
	 * @param method
	 *            HTTP method
	 */
	@Override
	public void setMethod(int method)
	{
		this.method = method;
	}

	/**
	 * return status code.<BR/>
	 *
	 * @return status code
	 */
	@Override
	public int getStatusCode()
	{
		return statusCode;
	}

	/**
	 * set status code.<BR/>
	 *
	 * @param statusCode
	 *            status code
	 */
	@Override
	public void setStatusCode(int statusCode)
	{
		this.statusCode = statusCode;
	}

	/**
	 * return header list.<BR/>
	 *
	 * @return headers map
	 */
	public Map<String, String> getHeaders()
	{

		return headers;
	}

	/**
	 * set header list.<BR/>
	 *
	 * @param headers
	 *            headers map
	 */
	public void setHeaders(Map<String, String> headers)
	{
		this.headers = headers;
	}

	/**
	 * return text encoding.<BR/>
	 *
	 * @return text encoding
	 */
	@Override
	public String getTextEncoding()
	{

		return textEncoding;
	}

	public void setTextEncoding(String textEncoding)
	{

		this.textEncoding = textEncoding;
	}

	public Map<String, String> getParameters()
	{

		return parameters;
	}

	public void setParameters(Map<String, String> parameters)
	{

		this.parameters = parameters;
	}

	public ContentDisposition getContentDisposition()
	{
		return contentDisposition;
	}

	public void setContentDisposition(ContentDisposition contentDisposition)
	{
		this.contentDisposition = contentDisposition;
	}

	public void setUrl(URL url) throws MalformedURLException
	{
		this.url = url;
		setProtocol(this.url.getProtocol().toLowerCase());
	}

	@Override
	public URL getUrl()
	{
		return url;
	}

	/**
	 * url을 설정한다.
	 *
	 * @param sUrl
	 *            url
	 */
	public void setUrl(String sUrl) throws MalformedURLException
	{
		url = new URL(sUrl);
		setUrl(url);
	}

	/**
	 * URL string을 리턴한다.
	 *
	 * @return URL string
	 */
	@Override
	public String getUrlString()
	{
		return url.toString();
	}

	/**
	 * protocol을 설정한다.
	 *
	 * @param protocol
	 */
	public void setProtocol(String protocol) throws MalformedURLException
	{
		if (protocol.equals("http"))
		{
			isHttps = false;
		}
		else if (protocol.equals("https"))
		{
			isHttps = true;
		}
		else
		{
			throw new MalformedURLException(
					"Not supported protocol: " + protocol);
		}
	}

	/**
	 * return use or not for SSL trusting.<BR/>
	 *
	 * @return use or not
	 */
	public boolean isSslTrust()
	{
		return isSslTrust;
	}

	/**
	 * set use or not for SSL trusting.<BR/>
	 *
	 * @param isSslTrust
	 *            use or not
	 */
	public void setSslTrust(boolean isSslTrust)
	{
		this.isSslTrust = isSslTrust;
	}

	/**
	 * return use or not for attaching file.<BR/>
	 *
	 * @return use or not
	 */
	@Override
	public boolean isExistAttachment()
	{
		return (contentDisposition != null && contentDisposition.getType()
				.equalsIgnoreCase(ContentDisposition.TYPE_ATTACHMENT)) ? true
						: false;
	}

	/**
	 * return attachement file name.<BR/>
	 *
	 * @return attach file name
	 */
	@Override
	public String getAttachmentFilename()
	{
		return contentDisposition
				.getParameterValue(ContentDisposition.PARM_FILENAME);
	}

	/**
	 * set attachment file name.<BR/>
	 *
	 * @param attachmentFileName
	 *
	 */
	public void setAttachmentFileName(String attachmentFileName)
	{
		contentDisposition.addParameter(ContentDisposition.PARM_FILENAME,
				attachmentFileName);
	}

	/**
	 * return body length<BR/>
	 *
	 * @return body length
	 */
	@Override
	public long getBodyLength()
	{
		return Long.parseLong(getHeaderValue("Content-Length"));
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer("=== ").append(name != null ? name : this.getClass().getName())
				.append("\n");
		if (statusCode != INetworkStatus.SC_UNDEFINED)
			sb.append("-status code: ").append(statusCode).append("\n");
		sb.append("-url: ").append(getUrlString()).append("\n");
		sb.append("-method: ")
				.append(DefaultHttpMessage.getMethodString(method))
				.append("\n");
		sb.append("-headers: ").append(headers).append("\n");
		if (contentDisposition != null)
		{
			sb.append("--").append(contentDisposition).append("\n");
		}
		if (parameters.size() > 0)
			sb.append("-parameters: ").append(parameters).append("\n");
		if (textEncoding != null)
			sb.append("-text-encoding: ").append(textEncoding).append("\n");
		if (bodyInputStream != null)
		{
			sb.append("-body:\n");
			String sBodyLength = getHeaderValue("Content-Length");
			int bodyLength = sBodyLength == null ? 0
					: Integer.parseInt(sBodyLength);
			if (bodyLength > BODY_PRINT_LENGTH_LIMIT)
			{
				sb.append(MESSAGE_BODY_LENGTHS_BIGGER);
			}
			else
			{
				sb.append(new String(getBodyByteArray())).append("\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Map<Integer, String> test main
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		Iterator<Map.Entry<Integer, String>> iterator = HttpStatusExtended.codeStrings
																.entrySet().iterator();
		Map.Entry e = null;
		while (iterator.hasNext())
		{
			e = iterator.next();
			System.out.println("CODE " + e.getKey() + ": "
									   + HttpStatusExtended.codeStrings.get(e.getKey()));
		}
	}

}
