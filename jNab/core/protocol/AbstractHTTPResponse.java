package jNab.core.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstract representation of HTTP responses sent to bunnies. The upload rate is throttled to prevent random behaviour from the bunny.
 * 
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public abstract class AbstractHTTPResponse
{
    /**
     * Throttle rate of uploads.
     */
    private static final int THROTTLE_RATE = 25;

    /**
     * Size of the upload buffer.
     */
    private static final int BUFFER_SIZE = 1000;

    /**
     * Output stream used to send the response to the bunny.
     */
    protected OutputStream out;

    /**
     * Creating a new response instance.
     * 
     * @param out the output stream used to send the response to the bunny.
     */
    protected AbstractHTTPResponse(OutputStream out)
    {
	this.out = out;
    }

    /**
     * Internal method sending response through the output stream used to communicate with the bunny.
     * 
     * @param in the input stream where to read data to send.
     * @throws IOException if some I/O error occurs while writing response data.
     * 
     */
    protected final void sendResponse(InputStream in) throws IOException
    {
	while (true)
	{
	    int available = in.available();

	    if (available > 0)
	    {
		int bufferSize = available;
		if (available > BUFFER_SIZE) bufferSize = BUFFER_SIZE;
		byte[] buffer = new byte[bufferSize];
		in.read(buffer);
		this.out.write(buffer);
		this.out.flush();
		try
		{
		    Thread.sleep(THROTTLE_RATE);
		}
		catch (InterruptedException e)
		{
		    // Ignoring this exception
		}
		continue;
	    }
	    break;
	}
    }
}
