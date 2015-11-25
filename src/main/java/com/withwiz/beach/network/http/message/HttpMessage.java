package com.withwiz.beach.network.http.message;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * IHttpMessage interface implements.<BR/>
 * Created by uni4love on 2010.05.08..
 */
abstract public class HttpMessage extends HttpStatusExtended implements IHttpMessage
{
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
     * body type: undefined
     */
    public static final int BODY_TYPE_UNDEFINED = -1;

    /**
     * body type: string
     */
    public static final int BODY_TYPE_STRING = 1;

    /**
     * body type: byte[]
     */
    public static final int BODY_TYPE_BYTE_ARRAY = 2;

    /**
     * multipart
     */
    public static final int BODY_TYPE_MULTIPART = 3;

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

}
