package jNab.core.protocol;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Class used to send a local file response.
 * 
 * @author Juha-Pekka Rajaniemi
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class LocalFileHTTPResponse extends AbstractHTTPResponse
{

    /**
     * Creating a new response instance, in order to send a local file to the bunny.
     * 
     * @param out the output stream used to communicate with the bunny.
     * @param file the path of the file to send.
     * @throws IOException if an IO error occured while sending response data bytes.
     */
    public LocalFileHTTPResponse(OutputStream out, File file) throws IOException
    {
	super(out);

	FileInputStream fis = null;
	try
	{
	    fis = new FileInputStream(file);
	}
	catch (FileNotFoundException e)
	{
	    String httpHeader = "HTTP/1.0 404 Not Found\r\n" + "\r\n";
	    byte[] httpHeaderBytes = null;
	    try
	    {
		httpHeaderBytes = httpHeader.getBytes("US-ASCII");
	    }
	    catch (Exception e1)
	    {
		// Ignoring exceptions here since a response is sent to the bunny
	    }
	    ByteArrayInputStream bis = new ByteArrayInputStream(httpHeaderBytes);
	    this.sendResponse(bis);
	    try
	    {
		bis.close();
	    }
	    catch (IOException e1)
	    {
		// Ignoring this exception
	    }
	    return;
	}

	String httpHeader = "HTTP/1.0 200 OK\r\n" + "Content-length: " + file.length() + "\r\n" + "\r\n";
	byte[] httpHeaderBytes = null;
	try
	{
	    httpHeaderBytes = httpHeader.getBytes("US-ASCII");
	}
	catch (UnsupportedEncodingException e1)
	{
	    // This exception can not occur since every platform should support ASCII encoding
	}

	this.out.write(httpHeaderBytes);
	out.flush();

	this.sendResponse(fis);

	try
	{
	    fis.close();
	}
	catch (IOException e)
	{
	    // Ignoring this exception
	}
    }
}
