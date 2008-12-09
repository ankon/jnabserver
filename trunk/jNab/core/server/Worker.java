package jNab.core.server;

import jNab.core.bunny.Bunny;
import jNab.core.choreography.Choreography;
import jNab.core.exceptions.MalformedRequestException;
import jNab.core.exceptions.NoSuchBunnyException;
import jNab.core.exceptions.NoSuchChoreographyException;
import jNab.core.protocol.AmbientBlock;
import jNab.core.protocol.ChoreographyHTTPResponse;
import jNab.core.protocol.HTTPRequest;
import jNab.core.protocol.LocalFileHTTPResponse;
import jNab.core.protocol.LocateHTTPResponse;
import jNab.core.protocol.Packet;
import jNab.core.protocol.PacketHTTPResponse;
import jNab.core.protocol.PingIntervalBlock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;

/**
 * Thread handling a client connection.
 * 
 * @author Juha-Pekka Rajaniemi
 * @author Ville Antila
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class Worker extends Thread
{
    /**
     * Socket used to communicate with the client.
     */
    private Socket clientSocket;

    /**
     * Micro server from which the worker has been created.
     */
    private MicroServer microServer;

    /**
     * Creating a new thread handling a client connection.
     * 
     * @param socket the socket used to communicate with the client.
     * @param microServer the micro server from which this worker has been created.
     */
    public Worker(Socket socket, MicroServer microServer)
    {
	// Thread name is remote socket address
	super(socket.getRemoteSocketAddress().toString());

	this.clientSocket = socket;
	this.microServer = microServer;

	// Setting the thread priority down so that the ServerSocket
	// will be responsive to new clients.
	this.setPriority(NORM_PRIORITY - 1);
    }

    /**
     * Internal method used to close client socket.
     */
    private void closeClientSocket()
    {
	try
	{
	    this.clientSocket.close();
	}
	catch (IOException e)
	{}
    }

    /**
     * @see Thread#run()
     */
    public void run()
    {
	InputStream inStream = null;
	OutputStream outStream = null;

	try
	{
	    // Getting the input stream used to read from client socket
	    inStream = this.clientSocket.getInputStream();

	    // Getting the output stream used to write to client socket
	    outStream = this.clientSocket.getOutputStream();
	}
	catch (IOException e2)
	{
	    try
	    {
		this.microServer.getDebugLoggingStream().println(
			"<jNab/server/worker:" + this.getName() + "> Unable to communicate with remote client");
	    }
	    catch (NullPointerException e3)
	    {}
	    this.closeClientSocket();
	    return;
	}

	// Creating a request object to wrap incoming HTTP request
	HTTPRequest r = null;
	try
	{
	    r = new HTTPRequest(inStream);
	}
	catch (MalformedRequestException e1)
	{
	    try
	    {
		this.microServer.getDebugLoggingStream().println("<jNab/server/worker:" + this.getName() + "> Malformed request");
	    }
	    catch (NullPointerException e3)
	    {}
	    this.closeClientSocket();
	    return;
	}

	try
	{
	    this.microServer.getDebugLoggingStream().println("<jNab/server/worker:" + this.getName() + "> Request received:\n" + r);
	}
	catch (NullPointerException e3)
	{}

	// Processing request

	// Case 1: bunny asks for its bootcode
	if (r.getURLParam("requestfile").equals("/vl/bc.jsp"))
	{
	    try
	    {
		this.microServer.getInfoLoggingStream().println("<jNab/server/worker:" + this.getName() + "> Sending boot code to bunny");
	    }
	    catch (NullPointerException e3)
	    {}
	    File bootcodeFile = new File(this.microServer.getResourcesPath(), "bootcode.bin");
	    try
	    {
		new LocalFileHTTPResponse(outStream, bootcodeFile);
	    }
	    catch (IOException e)
	    {
		try
		{
		    this.microServer.getDebugLoggingStream().println(
			    "<jNab/server/worker:" + this.getName() + "> Failed to send bootcode to bunny");
		}
		catch (NullPointerException e3)
		{}
	    }
	}

	// Case 2: bunny asks for location

	else if (r.getURLParam("requestfile").equals("/vl/locate.jsp"))
	{
	    // Sending the IP/port of the server
	    try
	    {
		this.microServer.getInfoLoggingStream().println("<jNab/server/worker:" + this.getName() + "> Sending logging information");
	    }
	    catch (NullPointerException e3)
	    {}
	    try
	    {
		new LocateHTTPResponse(outStream, this.microServer.getAddress(), this.microServer.getPort());
	    }
	    catch (IOException e)
	    {
		try
		{
		    this.microServer.getDebugLoggingStream().println(
			    "<jNab/server/worker:" + this.getName() + "> Failed to send location information to bunny");
		}
		catch (NullPointerException e3)
		{}
	    }
	}

	// Case 3: bunny notifies an event (ping, rfid, record, ...)

	else if (r.getURLParam("requestfile").equals("/vl/p4.jsp") || r.getURLParam("requestfile").equals("/vl/rfid.jsp")
		|| r.getURLParam("requestfile").equals("/vl/record.jsp"))
	{
	    // Retrieving bunny from the serial number included in the request
	    Bunny bunny = null;

	    String serialNumber = null;

	    // Checking if serial number seems correct
	    try
	    {
		serialNumber = r.getURLParam("sn");
	    }
	    catch (NoSuchElementException e)
	    {
		try
		{
		    this.microServer.getDebugLoggingStream().println(
			    "<jNab/server/worker:" + this.getName() + "> Unable to find valid serial number in request, ignoring it");
		}
		catch (NullPointerException e3)
		{}
		this.closeClientSocket();
		return;
	    }

	    // Retrieving bunny, or adding a new one
	    try
	    {
		bunny = this.microServer.getBurrow().getBunny(serialNumber);
	    }
	    catch (NoSuchBunnyException e)
	    {
		try
		{
		    this.microServer.getInfoLoggingStream().println(
			    "<jNab/server/worker:" + this.getName() + "> Adding the bunny whose serial number is " + serialNumber
				    + " to the burrow");
		}
		catch (NullPointerException e3)
		{}
		bunny = new Bunny(serialNumber);
		this.microServer.getBurrow().addBunny(bunny);
	    }

	    // Processing request
	    try
	    {
		this.microServer.getDebugLoggingStream().println("<jNab/server/worker:" + this.getName() + "> Processing request");
	    }
	    catch (NullPointerException e3)
	    {}
	    bunny.handleRequest(r);

	    // Sending the first packet in queue to the bunny
	    try
	    {
		Packet packet = bunny.getNextPacket();

		if (packet == null)
		{
		    try
		    {
			this.microServer.getDebugLoggingStream().println(
				"<jNab/server/worker:" + this.getName() + "> Packet to send is a default packet");
		    }
		    catch (Exception e)
		    {}

		    // If there was no packet in the list, a default packet is returned.
		    packet = new Packet();
		    packet.addBlock(new PingIntervalBlock(bunny.getPingInterval()));
		    packet.addBlock(new AmbientBlock());
		}

		try
		{
		    this.microServer.getDebugLoggingStream().println(
			    "<jNab/server/worker:" + this.getName() + "> Sending packet:\n" + packet);
		}
		catch (Exception e)
		{}
		new PacketHTTPResponse(outStream, packet);
	    }
	    catch (IOException e)
	    {
		try
		{
		    this.microServer.getDebugLoggingStream().println(
			    "<jNab/server/worker:" + this.getName() + "> Failed to send packet to bunny");
		}
		catch (NullPointerException e3)
		{}
	    }
	}

	// Case 4: bunny asks for a choreography
	else if (r.getURLParam("requestfile").startsWith("/chorlibrary/"))
	{
	    // Retrieving choreography
	    String choreographyName = r.getURLParam("requestfile").substring(13);
	    Choreography choreography = null;
	    try
	    {
		choreography = this.microServer.getChoregraphyLibrary().getChoreography(choreographyName);
	    }
	    catch (NoSuchChoreographyException e)
	    {
		try
		{
		    this.microServer.getDebugLoggingStream().println(
			    "<jNab/server/worker:" + this.getName() + "> bunny asked an unknown choreography (" + choreographyName + ")");
		}
		catch (NullPointerException e3)
		{}
		this.closeClientSocket();
		return;
	    }

	    try
	    {
		this.microServer.getDebugLoggingStream().println(
			"<jNab/server/worker:" + this.getName() + "> sending choreography (" + choreographyName + ") to bunny");
	    }
	    catch (NullPointerException e3)
	    {}
	    try
	    {
		new ChoreographyHTTPResponse(outStream, choreography);
	    }
	    catch (IOException e)
	    {
		try
		{
		    this.microServer.getDebugLoggingStream().println(
			    "<jNab/server/worker:" + this.getName() + "> failed to send choreography (" + choreographyName + ") to bunny");
		}
		catch (NullPointerException e3)
		{}
	    }
	}

	/*
	 * Case 5: (default) bunny asks for a local file
	 */
	else
	{
	    String fileName = null;
	    try
	    {
		fileName = r.getURLParam("requestfile").substring(1);
	    }
	    catch (NoSuchElementException e)
	    {
		try
		{
		    this.microServer.getDebugLoggingStream().println(
			    "<jNab/server/worker:" + this.getName() + "> malformed local file request");
		}
		catch (NullPointerException e3)
		{}
		this.closeClientSocket();
		return;
	    }

	    // Sending local file
	    try
	    {
		this.microServer.getDebugLoggingStream().println(
			"<jNab/server/worker:" + this.getName() + "> sending local file (" + fileName + ") to bunny");
	    }
	    catch (NullPointerException e3)
	    {}
	    try
	    {
		new LocalFileHTTPResponse(outStream, new File(fileName));
	    }
	    catch (IOException e)
	    {
		try
		{
		    this.microServer.getDebugLoggingStream().println(
			    "<jNab/server/worker:" + this.getName() + "> failed to send local file to bunny");
		}
		catch (NullPointerException e3)
		{}
	    }
	}
	this.closeClientSocket();
    }
}
