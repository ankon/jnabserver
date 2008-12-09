package jNab.core.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Class used to send a packet back to a bunny.
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class PacketHTTPResponse extends AbstractHTTPResponse
{
    /**
     * Sending a packet to the Nabaztag.
     * 
     * @param out the stream to communicate with the bunny.
     * @param packet the packet to send.
     * @throws IOException if an IO error occured while sending response data bytes.
     */
    public PacketHTTPResponse(OutputStream out, Packet packet) throws IOException
    {
	super(out);

	// Generating packet data
	byte[] data = packet.generatePacket();

	// Buffering header
	String httpHeader = "HTTP/1.0 200 OK\r\n" + "Content-length: " + data.length + "\r\n" + "\r\n";
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
	this.out.flush();

	ByteArrayInputStream bis = new ByteArrayInputStream(data);
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
