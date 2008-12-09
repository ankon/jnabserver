package jNab.core.server;

import jNab.core.bunny.Burrow;
import jNab.core.choreography.ChoreographyLibrary;
import jNab.core.plugins.PluginFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Front-end server.
 * 
 * @author Juha-Pekka Rajaniemi
 * @author Ville Antila
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class MicroServer extends Thread
{
    /**
     * IP address to which the server is bound.
     */
    private String address;

    /**
     * Port to which the server is bound.
     */
    private int port;

    /**
     * Indicator used to notify if the server has to continue its execution.
     */
    private volatile boolean keepRunning;

    /**
     * Indicator used to know if the server is running.
     */
    private boolean isRunning;

    /**
     * Burrows of bunnies.
     */
    private Burrow burrow;

    /**
     * Plugin manager.
     */
    private PluginFactory pluginFactory;

    /**
     * Path where to find resources (bootcode, local files, ...)
     */
    private File resourcesPath;

    /**
     * Path where to find plugins jar files
     */
    private File pluginsPath;

    /**
     * Choreography library used by the server.
     */
    private ChoreographyLibrary choreographyLibrary;

    /**
     * Error logging stream.
     */
    private PrintStream errorLogging;

    /**
     * Info logging stream.
     */
    private PrintStream infoLogging;

    /**
     * Debug logging stream.
     */
    private PrintStream debugLogging;

    /**
     * Creating a new server instance, bound on a given IP address and listening to a given port (server is not started).
     * 
     * @param address IP address of the server.
     * @param port port to which the server listens.
     * @param pluginsPath path where to find plugins jar files.
     * @param resourcesPath path where to find resources.
     * 
     */
    public MicroServer(String address, int port, File resourcesPath, File pluginsPath)
    {
	this.port = port;
	this.address = address;
	this.keepRunning = true;
	this.isRunning = false;
	this.resourcesPath = resourcesPath;
	this.pluginsPath = pluginsPath;
	this.pluginFactory = new PluginFactory(this.pluginsPath);
	this.burrow = new Burrow();
	this.burrow.setMicroServer(this);
	this.choreographyLibrary = new ChoreographyLibrary();
	this.errorLogging = null;
	this.infoLogging = null;
	this.debugLogging = null;
    }

    /**
     * @see Thread#run()
     */
    public void run()
    {
	this.isRunning = true;
	this.keepRunning = true;

	ServerSocket server_socket = null;

	// Creating a ServerSocket to listen for clients
	try
	{
	    if (this.address.equals(""))
		server_socket = new ServerSocket(this.port);
	    else
	    {
		server_socket = new ServerSocket();
		server_socket.bind(new InetSocketAddress(this.address, this.port));
	    }
	    try
	    {
		this.infoLogging.println("<jNab/server> Server started and listening to " + this.address + ":" + this.port);
	    }
	    catch (NullPointerException e)
	    {}

	    // Loop until server is claimed to be stopped
	    while (this.keepRunning)
	    {
		// Waiting for clients
		Socket client_socket = server_socket.accept();

		try
		{
		    this.debugLogging.println("<jNab/server> new connection from " + client_socket.getRemoteSocketAddress());
		}
		catch (NullPointerException e)
		{}

		// Starting a new thread handling client connection
		new Worker(client_socket, this).start();
	    }
	}
	catch (IOException e)
	{
	    try
	    {
		this.errorLogging.println("<jNab/server> Server execution failure : can't bind to port " + this.port);
	    }
	    catch (NullPointerException e2)
	    {}

	}

	// Closing socket
	try
	{
	    server_socket.close();
	}
	catch (IOException e)
	{}

	this.isRunning = false;

	try
	{
	    this.infoLogging.println("<jNab/server> Server stopped");
	}
	catch (NullPointerException e)
	{}
	return;
    }

    /**
     * Stopping the server.
     */
    public void stopServer()
    {
	try
	{
	    this.infoLogging.println("<jNab/server> Stopping server");
	}
	catch (NullPointerException e)
	{}

	this.keepRunning = false;

	// As the thread can be blocked on 'accept()',
	// A last client connection has to be simulated on order to complete
	// the shutdown of the server
	try
	{
	    Socket s = new Socket(this.address, this.port);
	    s.close();
	}
	catch (Exception e)
	{}
    }

    /**
     * Getting the status of the server.
     * 
     * @return <tt>true</tt> if the server is running, <tt>false</tt> if the server has been stopped.
     */
    public boolean isRunning()
    {
	return this.isRunning;
    }

    /**
     * Getting the IP address where the server is bound.
     * 
     * @return the IP address where the server is bound.
     */
    public String getAddress()
    {
	return this.address;
    }

    /**
     * Getting the port to which the server is listening.
     * 
     * @return the port to which the server is listening.
     */
    public int getPort()
    {
	return this.port;
    }

    /**
     * Getting NabazTag/Tag information repository.
     * 
     * @return NabazTag/Tag information repository.
     */
    public Burrow getBurrow()
    {
	return this.burrow;
    }

    /**
     * @see java.lang.Thread#toString()
     */
    public String toString()
    {
	StringBuffer result = new StringBuffer("jNab front-end server, bound to " + this.address + ":" + this.port);
	if (this.isRunning)
	    result.append(", started");
	else
	    result.append(", halted");
	return result.toString();
    }

    /**
     * Getting the plugin manager used by the server to instanciate plugins.
     * 
     * @return the plugin manager used by the server to instanciate plugins.
     */
    public PluginFactory getPluginFactory()
    {
	return this.pluginFactory;
    }

    /**
     * Getting the choreography manager used by the server.
     * 
     * @return the choreography manager used by the server.
     */
    public ChoreographyLibrary getChoregraphyLibrary()
    {
	return this.choreographyLibrary;
    }

    /**
     * Setting resources path.
     * 
     * @param resourcePath resources path.
     */
    public void setResourcePath(File resourcePath)
    {
	this.resourcesPath = resourcePath;
    }

    /**
     * Getting resources path.
     * 
     * @return resources path.
     */
    public File getResourcesPath()
    {
	return this.resourcesPath;
    }

    /**
     * Binding the server to a given IP address and a given port.
     * 
     * @param address the IP address where to bind the server.
     * @param port the port where to bind the server.
     */
    public void bindTo(String address, int port)
    {
	this.address = address;
	this.port = port;
    }

    /**
     * Setting the error logging stream.
     * 
     * @param stream the error logging stream.
     */
    public void setErrorLoggingStream(PrintStream stream)
    {
	this.errorLogging = stream;
    }

    /**
     * Getting the error logging stream.
     * 
     * @return the error logging stream.
     */
    public PrintStream getErrorLoggingStream()
    {
	return this.errorLogging;
    }

    /**
     * Setting the info logging stream.
     * 
     * @param stream the info logging stream.
     */
    public void setInfoLoggingStream(PrintStream stream)
    {
	this.infoLogging = stream;
    }

    /**
     * Getting the info logging stream.
     * 
     * @return the info logging stream.
     */
    public PrintStream getInfoLoggingStream()
    {
	return this.infoLogging;
    }

    /**
     * Setting the debug logging stream.
     * 
     * @param stream the debug logging stream.
     */
    public void setDebugLoggingStream(PrintStream stream)
    {
	this.debugLogging = stream;
    }

    /**
     * Getting the debug logging stream.
     * 
     * @return the debug logging stream.
     */
    public PrintStream getDebugLoggingStream()
    {
	return this.debugLogging;
    }
}
