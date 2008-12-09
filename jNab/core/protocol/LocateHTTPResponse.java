package jNab.core.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Class used ot send a localization response.
 * 
 * @author Juha-Pekka Rajaniemi
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class LocateHTTPResponse extends AbstractHTTPResponse
{
    /**
     * Creating a new response instance, in order to send server's address to the bunny.<br/> N.B the same server (i.e. the micro server) is
     * used as broadcast and ping server.
     * 
     * @param out the output stream used to communicate with the bunny.
     * @param ipAddress the IP address to which ping/broad server is bound.
     * @param port the port to which ping/broad server is bound.
     * @throws IOException if an IO eroor occured while sending response data bytes.
     */
    public LocateHTTPResponse(OutputStream out, String ipAddress, int port) throws IOException
    {
	super(out);
	String httpHeader = "HTTP/1.0 200 OK\r\n\r\n" + "ping " + ipAddress + ":" + port + "\r\n" + "broad " + ipAddress + ":" + port
		+ "\r\n\r\n";
	byte[] httpHeaderBytes = null;
	try
	{
	    httpHeaderBytes = httpHeader.getBytes("US-ASCII");
	}
	catch (UnsupportedEncodingException e)
	{
	    // This exception can not occur since every platform should support ASCII encoding.
	}
	this.sendResponse(new ByteArrayInputStream(httpHeaderBytes));
    }
}
