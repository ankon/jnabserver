package jNab.core.protocol;

import jNab.core.choreography.Choreography;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Class used to send a choreography response.
 * 
 * @author Juha-Pekka Rajaniemi
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class ChoreographyHTTPResponse extends AbstractHTTPResponse
{
    /**
     * Creating a new response instance, aiming to make the bunny playing a choreography.
     * 
     * @param out the stream used to communicate with the bunny.
     * @param choreography the choreography to send.
     * @throws IOException if an IO error occurs while sending response bytes.
     * 
     */
    public ChoreographyHTTPResponse(OutputStream out, Choreography choreography) throws IOException
    {
	super(out);

	// Retrieving choreography data (excluding header and footer)
	byte[] data = choreography.getData();

	// Writing HTTP status line
	String httpHeader = "HTTP/1.0 200 OK\r\n" + "Content-length: " + (data.length + 8) + "\r\n" + "\r\n";
	byte[] httpHeaderBytes = null;
	try
	{
	    httpHeaderBytes = httpHeader.getBytes("US-ASCII");
	}
	catch (UnsupportedEncodingException e)
	{
	    // This exception can not occur since every platform should support ASCII encoding.
	}
	this.out.write(httpHeaderBytes);
	out.flush();

	// Writing choreography data (including header and footer)

	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	DataOutputStream dos = new DataOutputStream(bos);
	// Appending header
	dos.writeInt(data.length);
	// Appending data
	dos.write(data);
	// Appending footer
	dos.writeInt(0);
	dos.flush();

	// Getting full choreography data as a byte array
	ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

	try
	{
	    dos.close();
	    bos.close();
	    bis.close();
	}
	catch (IOException e)
	{
	    // Ignoring this exception
	}

	// Sending choreography data
	this.sendResponse(bis);

	try
	{
	    bis.close();
	}
	catch (IOException e)
	{
	    // Ignoring this exception
	}
    }
}
