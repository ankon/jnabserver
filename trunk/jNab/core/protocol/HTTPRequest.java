package jNab.core.protocol;

import jNab.core.exceptions.MalformedRequestException;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * Wrapping object for requests coming from bunnies.
 * 
 * @author Juha-Pekka Rajaniemi
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class HTTPRequest extends AbstractRequest
{
    /**
     * Method of the request, either POST or GET.
     */
    private String method;

    /**
     * URL invoked in this request.
     */
    private String url;

    /**
     * Parsed parameters from the URL.
     */
    private Properties urlParams;

    /**
     * Parsed fields from the header.
     */
    private Properties headerFields;

    /**
     * Byte array containing the POST data.
     */
    private byte[] postData;

    /**
     * Creating a new HTTP request instance by reading the data from an input stream and parsing it.
     * 
     * @param in the stream used to read the request.
     * @throws MalformedRequestException if the data read from the input stream is not a well-formed request.
     */
    public HTTPRequest(InputStream in) throws MalformedRequestException
    {
	super(in);
	this.protocol = "HTTP";
    }

    /**
     * @see jNab.core.protocol.AbstractRequest#readIncomingRequestFromInputStream(java.io.InputStream)
     */
    protected void readIncomingRequestFromInputStream(InputStream in) throws MalformedRequestException
    {
	try
	{
	    char c;
	    boolean isFirstLineRead = false;
	    String line = "";
	    this.headerFields = new Properties();

	    // Reading the header fields
	    while ((c = (char) in.read()) != -1)
	    {
		line += (char) c;

		// End of header fields
		if (line.equals("\r\n")) break;

		// New line
		if (line.endsWith("\r\n"))
		{
		    // Stripping the \r\n
		    line = line.substring(0, line.length() - 2);
		    // Request line
		    if (!isFirstLineRead)
		    {
			this.headerFields.setProperty("request", line);
			line = "";
			isFirstLineRead = true;
		    }
		    // Http header parameter
		    else
		    {
			String name = line.substring(0, line.indexOf(':'));
			String value = line.substring(line.indexOf(':') + 1).trim();
			this.headerFields.setProperty(name, value);
			line = "";
		    }
		}

	    }

	    // Splitting the message into substrings.
	    String[] tokens = this.headerFields.getProperty("request").split(" ");

	    // if there is incoming data, saving it (e.g. recorded data)
	    if (this.headerFields.getProperty("Content-length") != null)
	    {

		try
		{
		    int contentLength = Integer.parseInt(this.headerFields.getProperty("Content-length"));

		    BufferedInputStream bis = new BufferedInputStream(in, 4096);
		    this.postData = new byte[contentLength];

		    for (int j = 0; j < contentLength; j++)
			this.postData[j] = (byte) bis.read();

		}
		catch (Throwable e)
		{
		    throw new MalformedRequestException();
		}
	    }

	    // Parsing the URL
	    this.urlParams = new Properties();
	    this.url = tokens[1];
	    this.protocol = tokens[2];
	    String[] urlParts = tokens[1].split("\\?");

	    this.urlParams.setProperty("requestfile", urlParts[0]);
	    this.method = tokens[0];

	    // Parsing URL parameters
	    if (urlParts.length > 1)
	    {
		String[] params = urlParts[1].split("&");

		for (String element : params)
		{
		    String[] params2 = element.split("=");
		    // if the value if empty
		    String value = "";
		    if (params2.length == 2) value = params2[1];
		    this.urlParams.setProperty(params2[0], value);
		}
	    }
	}
	catch (Throwable e)
	{
	    throw new MalformedRequestException();
	}

    }

    /**
     * Getting an header field value.
     * 
     * @param headerFieldName the name of the header field.
     * @return the value of the header field.
     * @throws NoSuchElementException if the header field does not exists.
     */
    public String getHeaderParam(String headerFieldName) throws NoSuchElementException
    {
	String value = null;
	try
	{
	    value = this.headerFields.getProperty(headerFieldName);
	}
	catch (NullPointerException e)
	{
	    throw new NoSuchElementException(headerFieldName);
	}
	return value;
    }

    /**
     * Getting the type of the request.
     * 
     * @return the type of the request (either POST or GET).
     */
    public String getMethod()
    {
	return this.method;
    }

    /**
     * Getting the requested URL.
     * 
     * @return the requested URL.
     */
    public String getURL()
    {
	return this.url;
    }

    /**
     * Getting an URL parameter value.
     * 
     * @param urlParameterName the name of the parameter.
     * @return the value of the parameter.
     * @throws NoSuchElementException if the parameter does not exists.
     */
    public String getURLParam(String urlParameterName) throws NoSuchElementException
    {

	String value = null;
	try
	{
	    value = this.urlParams.getProperty(urlParameterName);
	}
	catch (NullPointerException e)
	{
	    throw new NoSuchElementException(urlParameterName);
	}
	return value;
    }

    /**
     * Getting the POST data.
     * 
     * @return the POST data.
     */
    public byte[] getPostData()
    {
	return this.postData;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
	return "Request with params: " + this.urlParams.toString();
    }
}
