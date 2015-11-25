package com.withwiz.beach.network.http.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Vector;

import javax.net.ssl.SSLException;

import com.withwiz.beach.network.http.message.*;
import com.withwiz.beach.network.http.message.DefaultHttpMessage;
import com.withwiz.beach.network.http.ssl.CertPassSSLSocketFactory;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.withwiz.plankton.io.ProxyInputStream;

/**
 * This implements IHttpProcessor interface with org.apache.http.client.HttpClient.<BR/>
 * Created by uni4love on 2010. 5. 8.
 */
public class DefaultHttpProcessor
		implements IHttpProcessor<DefaultHttpRequestMessage, DefaultHttpResponseMessage>
{
	/**
	 * logger
	 */
	Logger log = LoggerFactory.getLogger(DefaultHttpProcessor.class);

	/**
	 * use writing response to file or not.<BR/>
	 */
	private boolean isWritingResponseToFile = false;

	/**
	 * file path for saving a response
	 */
	private String responseFilePath = null;

	/**
	 * proxy use or not for reusing response inputstream
	 */
	private boolean isProxyResponseData = false;

	/**
	 * connection timeout<br/>
	 * default: 10 seconds
	 */
	private int connectionTimeout = 5000;

	/**
	 * socket timeout<br/>
	 * default: 10 seconds
	 */
	private int socketTimeout = 10000;

	/**
	 * network data buffer size<br/>
	 * default: 8k
	 */
	private int networkBufferSize = 8192;

	/**
	 * HttpClient instance
	 */
	private HttpClient httpClient = null;

	/**
	 * HttpParams instance
	 */
	private HttpParams httpParameters = null;

	/**
	 * Set if HTTP requests are blocked from being executed on this thread
	 */
	private static final ThreadLocal<Boolean> threadBlocked = new ThreadLocal<Boolean>();

	/**
	 * Interceptor throws an exception if the executing thread is blocked
	 */
	private static final HttpRequestInterceptor	threadCheckInterceptor	= new HttpRequestInterceptor()
	{
		public void process(HttpRequest request, HttpContext context)
		{
			if (Boolean.TRUE.equals(threadBlocked.get()))
			{
				throw new RuntimeException("This thread forbids HTTP requests");
			}
		}
	};

	/**
	 * SSLSocketFactory
	 */
	private SSLSocketFactory					sslSocketFactory		= null;

	/**
	 * allow all SSLSocketFactory
	 */
	private static SSLSocketFactory allowAllSSLSocketFactory = null;

	static
	{
		try
		{
			KeyStore trustStore = KeyStore
					.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			allowAllSSLSocketFactory = new CertPassSSLSocketFactory(trustStore);
		}
		catch (KeyStoreException e)
		{
			e.printStackTrace();
		}
		catch (CertificateException e)
		{
			e.printStackTrace();
		}
		catch (UnrecoverableKeyException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (KeyManagementException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * constructor
	 */
	public DefaultHttpProcessor()
	{
		this(-1, -1);
	}

	/**
	 * constructor
	 *
	 * @param connectionTimeout
	 *            connection timeout
	 * @param socketTimeout
	 *            socket timeout
	 */
	public DefaultHttpProcessor(int connectionTimeout, int socketTimeout)
	{
		this(connectionTimeout, socketTimeout, -1);
	}

	/**
	 * constructor
	 *
	 * @param connectionTimeout
	 *            connection timeout
	 * @param socketTimeout
	 *            socket timeout
	 * @param networkBufferSize
	 *            network
	 */
	public DefaultHttpProcessor(int connectionTimeout, int socketTimeout,
								int networkBufferSize)
	{
		this(connectionTimeout, socketTimeout, networkBufferSize, null);
	}

	/**
	 * constructor
	 *
	 * @param sslSocketFactory
	 *            SSLSocketFactory
	 */
	public DefaultHttpProcessor(SSLSocketFactory sslSocketFactory)
	{
		this(-1, -1, -1, sslSocketFactory);
	}

	/**
	 * constructor
	 *
	 * @param connectionTimeout
	 *            connection timeout
	 * @param socketTimeout
	 *            socket timeout \* @param trustManager TrustManager
	 * @param sslSocketFactory
	 *            SSLSocketFactory
	 */
	public DefaultHttpProcessor(int connectionTimeout, int socketTimeout,
								SSLSocketFactory sslSocketFactory)
	{
		this(connectionTimeout, socketTimeout, -1, sslSocketFactory);
	}

	/**
	 * constructor
	 *
	 * @param connectionTimeout
	 *            connection timeout
	 * @param socketTimeout
	 *            socket timeout
	 * @param networkBufferSize
	 *            network
	 * @param sslSocketFactory
	 *            SSLSocketFactory
	 */
	public DefaultHttpProcessor(int connectionTimeout, int socketTimeout,
								int networkBufferSize, SSLSocketFactory sslSocketFactory)
	{
		if (connectionTimeout > 0)
		{
			this.connectionTimeout = connectionTimeout;
		}
		if (socketTimeout > 0)
		{
			this.socketTimeout = socketTimeout;
		}
		if (networkBufferSize > 0)
		{
			this.networkBufferSize = networkBufferSize;
		}
		if (sslSocketFactory == null)
		{
			this.sslSocketFactory = getDefaultSSLSocketFactory();
		}
		else
		{
			this.sslSocketFactory = sslSocketFactory;
		}
	}

	/**
	 * constructor
	 *
	 * @param isHttpTrust
	 *            HTTPS trust
	 */
	public DefaultHttpProcessor(boolean isHttpTrust)
	{
		this(-1, -1, -1, isHttpTrust == true ? getDefaultSSLSocketFactory()
				: allowAllSSLSocketFactory);
	}

	/**
	 * return default SSLSocketFactory
	 *
	 * @return SSLSocketFactory
	 */
	private static SSLSocketFactory getDefaultSSLSocketFactory()
	{
		return SSLSocketFactory.getSocketFactory();
	}

	/**
	 * This method create HttpClient instance.<BR/>
	 *
	 * @return HttpClient
	 */
	public HttpClient createHttpClient()
	{
		HttpClient httpClient = createHttpClient(null);
		return httpClient;
	}

	/**
	 * HttpClient instance를 생성한다.
	 *
	 * @return HttpClient
	 */
	public HttpClient createHttpClient(SSLSocketFactory sslSocketFactory)
	{
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setStaleCheckingEnabled(httpParameters, false);
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				connectionTimeout);
		HttpConnectionParams.setSoTimeout(httpParameters, socketTimeout);
		HttpConnectionParams.setSocketBufferSize(httpParameters,
				networkBufferSize);
		HttpClientParams.setRedirecting(httpParameters, false);
		// HttpProtocolParams.setUserAgent(httpParameters, userAgent);

		// socket factory
		// plain
		PlainSocketFactory plainSocketFactory = PlainSocketFactory
				.getSocketFactory();

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", plainSocketFactory, 80));
		schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
		// Thread safe client connection manager
		ClientConnectionManager connManager = new ThreadSafeClientConnManager(
				httpParameters, schemeRegistry);
		// create HttpClient instance
		HttpClient httpClient = new DefaultHttpClient(connManager,
				httpParameters)
		{
			@Override
			protected BasicHttpProcessor createHttpProcessor()
			{
				// Add interceptor to prevent making requests from main thread.
				BasicHttpProcessor processor = super.createHttpProcessor();
				processor.addRequestInterceptor(threadCheckInterceptor);
				return processor;
			}

			@Override
			protected HttpContext createHttpContext()
			{
				// Same as DefaultHttpClient.createHttpContext() minus the
				// cookie store.
				HttpContext context = new BasicHttpContext();
				context.setAttribute(ClientContext.AUTHSCHEME_REGISTRY,
						getAuthSchemes());
				context.setAttribute(ClientContext.COOKIESPEC_REGISTRY,
						getCookieSpecs());
				context.setAttribute(ClientContext.CREDS_PROVIDER,
						getCredentialsProvider());
				return context;
			}
		};

		log.debug("ConnectionTimeout: {}, SocketTimeout: {}, NetworkBuffer: {}",
				connectionTimeout, socketTimeout, networkBufferSize);
		return httpClient;
	}

	/**
	 * close this service.<BR/>
	 */
	public void close()
	{
		if (httpClient != null)
		{
			httpClient = null;
		}
	}

	/**
	 * return HTTP method instance.<BR/>
	 *
	 * @param message
	 *            DefaultHttpMessage
	 * @return HttpRequestBase
	 */
	private HttpRequestBase createRequestHttpMethod(DefaultHttpRequestMessage message)
			throws UnsupportedEncodingException
	{
		HttpRequestBase req = null;
		switch (message.getMethod())
		{
			case DefaultHttpMessage.METHOD_GET:
				req = new HttpGet(message.getUrlString());
				break;
			case DefaultHttpMessage.METHOD_POST:
				req = new HttpPost(message.getUrlString());
				break;
			case DefaultHttpMessage.METHOD_PUT:
				req = new HttpPut(message.getUrlString());
				break;
			case DefaultHttpMessage.METHOD_DELETE:
				req = new HttpDeleteWithBody(message.getUrlString());
				break;
		}

		if (message.getMethod() != DefaultHttpMessage.METHOD_GET)
		{
			// multipart use or not
			if (isMultipart(message))
			{
				MultipartEntity multipartEntity = new MultipartEntity();
				// file
				if (message.getAttachFilePath() != null
						&& message.getAttachFilePath().length() > 0)
				{
					// file
					File file = new File(message.getAttachFilePath());
					FileBody fileBody = new FileBody(file);
					multipartEntity.addPart(file.getName(), fileBody);
				}
				// parameter
				if (message.getParameters() != null
						&& message.getParameters().size() > 0)
				{
					// text(parameter)
					StringBody stringBody = null;
					for (String name : message.getParameterNames())
					{
						stringBody = new StringBody(
								message.getParameterValue(name), "text/plain",
								Charset.forName(message.getTextEncoding()));
						multipartEntity.addPart(name, stringBody);
					}
				}
				// // body content
				// if (message.getBodyByteArray() != null
				// && message.getBodyByteArray().length > 0)
				// {
				// // text(parameter)
				// StringBody stringBody = new StringBody(new String(
				// message.getBodyByteArray()), "text/plain",
				// Charset.forName(message.getTextEncoding()));
				// multipartEntity.addPart("text", stringBody);
				// }

				// inputstream
				if (message.getBodyInputStream() != null)
				{
					InputStreamBody inputStreamBody = new InputStreamBody(
							message.getBodyInputStream(),
							message.getBodyName());
					multipartEntity.addPart(message.getBodyName(),
							inputStreamBody);
				}
				((HttpEntityEnclosingRequestBase) req)
						.setEntity(multipartEntity);
			}
			else
			{
				HttpEntity entity = null;
				if (message.getBodyInputStream() != null)
				{
					if (message
							.getBodyType() == DefaultHttpRequestMessage.BODY_TYPE_STRING)
					{
						entity = new StringEntity(
								new String(message.getBodyByteArray()),
								message.getTextEncoding());
					}
					else if (message
							.getBodyType() == DefaultHttpRequestMessage.BODY_TYPE_BYTE_ARRAY)
					{
						entity = new ByteArrayEntity(
								message.getBodyByteArray());
					}
				}
				else if (message.getParameterSize() > 0)
				{
					Vector<NameValuePair> nameValuePair = new Vector<NameValuePair>();
					for (String name : message.getParameterNames())
					{
						nameValuePair.add(new BasicNameValuePair(name,
								message.getParameterValue(name)));
					}
					try
					{
						entity = new UrlEncodedFormEntity(nameValuePair);
					}
					catch (UnsupportedEncodingException e)
					{
						e.printStackTrace();
					}
				}
				((HttpEntityEnclosingRequestBase) req).setEntity(entity);
			}
		}

		// header
		String[] headerNames = message.getHeaderNames();
		for (String headerName : headerNames)
		{
			req.addHeader(headerName, message.getHeaderValue(headerName));
		}

		return req;
	}

	/**
	 * return use or not multipart.
	 *
	 * @param message
	 *            DefaultHttpRequestMessage
	 * @return use or not
	 */
	private boolean isMultipart(DefaultHttpRequestMessage message)
	{
		if (message.getAttachFilePath() != null
				&& message.getAttachFilePath().length() > 0)
		{
			return true;
		}
		if (message.getBodyType() == DefaultHttpRequestMessage.BODY_TYPE_MULTIPART)
		{
			return true;
		}
		return false;
	}

	/**
	 * process for HTTP request.<BR/>
	 *
	 * @param requestMessage
	 *            DefaultHttpRequestMessage
	 * @return DefaultHttpResponseMessage
	 */
	@Override
	public DefaultHttpResponseMessage request(DefaultHttpRequestMessage requestMessage)
	{
		DefaultHttpResponseMessage responseMessage = null;
		int status = -1;
		HttpUriRequest httpUriRequest = null;
		InputStream inputStream = null;
		Header[] responseHeaders = null;
		try
		{
			// create HttpRequest
			httpUriRequest = createRequestHttpMethod(requestMessage);
			log.debug("RequestMessage:\n{}", requestMessage.toString());
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return null;
		}
		// create httpclient
		if (httpClient == null)
		{
			// httpClient = createHttpClient();
			httpClient = createHttpClient(sslSocketFactory);
		}

		try
		{
			// execute httpclient
			HttpResponse response = httpClient.execute(httpUriRequest);
			status = response.getStatusLine().getStatusCode();
			responseHeaders = response.getAllHeaders();

			HttpEntity entity = response.getEntity();
			if (isProxyResponseData)
			{
				ProxyInputStream pis = new ProxyInputStream(
						entity.getContent());
				inputStream = pis.getNewInputStream();
			}
			else
			{
				inputStream = entity.getContent();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			if (httpUriRequest != null)
			{
				httpUriRequest.abort();
			}
			if (e instanceof UnknownHostException)
			{
				status = HttpStatusExtended.SC_UNKNOWN_HOST;
			}
			else if (e instanceof ConnectTimeoutException)
			{
				status = HttpStatusExtended.SC_CONNECTION_TIMEOUT;
			}
			else if (e instanceof SocketTimeoutException)
			{
				status = HttpStatusExtended.SC_SOCKET_TIMEOUT;
			}
			else if (e instanceof SocketException)
			{
				if (e.getMessage().toLowerCase().indexOf("timed out") != -1)
				{
					status = HttpStatusExtended.SC_SOCKET_TIMEOUT;
				}
				else if (e.getMessage().toLowerCase()
						.indexOf("network unreachable") != -1)
				{
					status = HttpStatusExtended.SC_NETWORK_UNREACHABLE;
				}
				else if (e.getMessage().toLowerCase()
						.indexOf("no route to host") != -1)
				{
					status = HttpStatusExtended.SC_NO_ROUTE_TO_HOST;
				}
				else if (e instanceof HttpHostConnectException)
				{
					status = HttpStatusExtended.SC_HTTP_HOST_CONNECTION_ERROR;
				}
				else
				{
					status = HttpStatusExtended.SC_SOCKET_ERROR;
				}
			}
			else if (e instanceof ClientProtocolException)
			{
				status = HttpStatusExtended.SC_CLIENT_PROTOCOL_CRASH_VALUE;
			}
			else if (e instanceof SSLException)
			{
				status = HttpStatusExtended.SC_HTTPS_ERROR;
			}
		}
		finally
		{
			responseMessage = createHttpResponseMessage(requestMessage, status,
					responseHeaders, inputStream);
			log.debug("ResponseMessage:\n{}", responseMessage.toString());
		}
		return responseMessage;
	}

	/**
	 * create Response message and return.
	 *
	 * @param requestMessage
	 *            requesst message
	 * @param status
	 *            status code
	 * @param bodyInputStream
	 *            InputStream to HTTP body
	 * @return DefaultHttpResponseMessage
	 */
	private DefaultHttpResponseMessage createHttpResponseMessage(
			DefaultHttpRequestMessage requestMessage, int status,
			Header[] headers, InputStream bodyInputStream)
	{
		DefaultHttpResponseMessage responseMessage = new DefaultHttpResponseMessage();
		responseMessage.setStatusCode(status);
		try
		{
			responseMessage.setUrl(requestMessage.getUrl());
		} catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		if (headers != null)
		{
			for (Header header : headers)
			{
				responseMessage.addHeaderParameter(header.getName(),
						header.getValue());
				if (header.getName().equalsIgnoreCase(
						ContentDisposition.CONTENT_DISPOSITION_NAME))
				{
					responseMessage.setContentDisposition(
							ContentDisposition
									.getContentDisposition(header.getValue()));
				}
			}
		}
		responseMessage.setBodyInputStream(bodyInputStream);
		responseMessage.setHttps(requestMessage.isHttps());
		responseMessage.setMethod(requestMessage.getMethod());
		responseMessage.setTextEncoding(requestMessage.getTextEncoding());

		return responseMessage;
	}

	/**
	 * return file path for saving response.<BR/>
	 *
	 * @return file path
	 */
	public String getResponseFilePath()
	{
		return responseFilePath;
	}

	/**
	 * set file path for saving response.<BR/>
	 *
	 * @param responseFilePath
	 *            file path
	 *
	 */
	public void setResponseFilePath(String responseFilePath)
	{
		this.responseFilePath = responseFilePath;
	}

	/**
	 * return use or not for saving response.<BR/>
	 *
	 * @return use or not
	 */
	public boolean isWritingResponseToFile()
	{
		return isWritingResponseToFile;
	}

	/**
	 * set use or not for saving response.<BR/>
	 *
	 * @param writingResponseToFile
	 *            use or not
	 */
	public void setWritingResponseToFile(boolean writingResponseToFile)
	{
		isWritingResponseToFile = writingResponseToFile;
	}

	/**
	 * return connection timeout.<BR/>
	 *
	 * @return connection timeout(milliseconds)
	 */
	public int getConnectionTimeout()
	{
		return connectionTimeout;
	}

	/**
	 * set connection timeout<BR/>
	 *
	 * @param connectionTimeout
	 *            connection timeout(milliseconds)
	 */
	public void setConnectionTimeout(int connectionTimeout)
	{
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * return socket timeout.<BR/>
	 *
	 * @return socket timeout(milliseconds)
	 */
	public int getSocketTimeout()
	{
		return socketTimeout;
	}

	/**
	 * set socket timeout.<BR/>
	 *
	 * @param socketTimeout
	 *            socket timeout(milliseconds)
	 */
	public void setSocketTimeout(int socketTimeout)
	{
		this.socketTimeout = socketTimeout;
	}

	/**
	 * return network buffer size.<BR/>
	 *
	 * @return network buffer size(bytes)
	 */
	public int getNetworkBufferSize()
	{
		return networkBufferSize;
	}

	/**
	 * set network buffer size.<BR/>
	 *
	 * @param networkBufferSize
	 *            network buffer size(bytes)
	 */
	public void setNetworkBufferSize(int networkBufferSize)
	{
		this.networkBufferSize = networkBufferSize;
	}

	/**
	 * return HttpClient.<BR/>
	 *
	 * @return HttpClient instance
	 */
	public HttpClient getHttpClient()
	{
		return httpClient;
	}

	/**
	 * set HttpClient instance.<BR/>
	 *
	 * @param httpClient
	 *            HttpClient instance
	 */
	public void setHttpClient(HttpClient httpClient)
	{
		this.httpClient = httpClient;
	}

	/**
	 * return use or not proxy for saving HTTP response.<BR/>
	 *
	 * @return use or not
	 */
	public boolean isProxyResponseData()
	{
		return isProxyResponseData;
	}

	/**
	 * set use or not proxy for saving HTTP response.<BR/>
	 *
	 * @param proxyResponseData
	 *            use or not
	 */
	public void setProxyResponseData(boolean proxyResponseData)
	{
		isProxyResponseData = proxyResponseData;
	}

	/**
	 * test main
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		Logger log = LoggerFactory.getLogger(DefaultHttpProcessor.class);

		log.info("=== DefaultHttpProcessor test ===");
		DefaultHttpProcessor http = new DefaultHttpProcessor();
		DefaultHttpRequestMessage req = new DefaultHttpRequestMessage();
		try
		{
			req.setUrl("http://www.withwiz.com");
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		String url = req.getUrlString();
		log.info("service url: {}", url);
		http.setProxyResponseData(false);
		DefaultHttpResponseMessage res = http.request(req);

		log.info("response code: {}", res.getStatusCode());
		switch (res.getStatusCode())
		{
			case HttpStatusExtended.SC_OK:
				log.info("response:\n{}", res.toString());
				break;
			default:
				log.info("response:\n{}", res.toString());
		}
	}
}