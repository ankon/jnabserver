package jNab.core.protocol;

import jNab.core.exceptions.MalformedRequestException;

import java.io.InputStream;

/**
 * Abstract representation of requests coming from bunnies, whatever the kind of protocol used.
 * 
 * @author Juha-Pekka Rajaniemi
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public abstract class AbstractRequest
{
    /**
     * Protocol used by the request, must be HTTP.
     */
    protected String protocol;

    /**
     * Creating a new request instance by reading the incoming request and parsing it.
     * 
     * @param in the stream used to read the request.
     * @throws MalformedRequestException if the data read from the input stream is not a well-formed request.
     */
    protected AbstractRequest(InputStream in) throws MalformedRequestException
    {
	this.readIncomingRequestFromInputStream(in);
    }

    /**
     * Internal method used to read a request from a input stream.
     * 
     * @param in the stream used to read the request.
     * @throws MalformedRequestException if the data read from the input stream is not a well-formed request.
     */
    protected abstract void readIncomingRequestFromInputStream(InputStream in) throws MalformedRequestException;

    /**
     * Getting the protocol used by the request.
     * 
     * @return The protocol.
     */
    public String getProtocol()
    {
	return this.protocol;
    }

}
