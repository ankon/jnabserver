package jNab.ext.configuration;

import jNab.core.bunny.Bunny;
import jNab.core.bunny.Burrow;
import jNab.core.exceptions.NoSuchBunnyException;
import jNab.core.exceptions.NoSuchPluginException;
import jNab.core.exceptions.PluginCreationException;
import jNab.core.plugins.AbstractPlugin;
import jNab.core.server.MicroServer;
import jNab.ext.persistency.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Set;

/**
 * Micro server configuration service using a TCP-based application protocol.
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class ServerConfigurationServer extends Thread
{
    /**
     * Connection state meaning that client connection must be kept open.
     */
    private final static int KEEP_ALIVE = 0;

    /**
     * Connection state meaning that client connection must be closed.
     */
    private final static int CLIENT_SHUTDOWN = 1;

    /**
     * Connection state meaning that server must be shut down.
     */
    private final static int SERVER_SHUTDOWN = 2;

    /**
     * Micro server to configure.
     */
    private MicroServer microServer;

    /**
     * Serialization service to use.
     */
    private Serializer serializer;

    /**
     * IP address where to bind the configuration service.
     */
    private String address;

    /**
     * Port where to bind the configuration service.
     */
    private int port;

    /**
     * Server socket used to accept clients.
     */
    private ServerSocket serverSocket;

    /**
     * Creating a new configuration service instance, bound to a given IP/port.
     * 
     * @param address the IP address where to bind the configuration service.
     * @param port the port where to bind the configuration service.
     * @param microServer the micro server to configure.
     * @param serializer the serialization service to use.
     */
    public ServerConfigurationServer(String address, int port, MicroServer microServer, Serializer serializer)
    {
	super();
	this.address = address;
	this.port = port;
	this.microServer = microServer;
	this.serializer = serializer;
    }

    /**
     * N.B. The configuration service is sequential (i.e. it accepts only one connection at a time).
     * 
     * @see java.lang.Thread#run()
     */
    public void run()
    {
	try
	{
	    this.serverSocket = new ServerSocket();
	    this.serverSocket.bind(new InetSocketAddress(this.address, this.port));
	}
	catch (IOException e)
	{
	    try
	    {
		this.microServer.getErrorLoggingStream().println(
			"<JNab configuration server> unable to create/bind configuration service socket");
	    }
	    catch (NullPointerException e2)
	    {}
	    return;
	}

	// Keeping the service running until someone tells to stop it
	while (true)
	{
	    // Waiting for a client connection
	    Socket socket = null;
	    try
	    {
		socket = this.serverSocket.accept();
	    }
	    catch (IOException e)
	    {
		try
		{
		    this.microServer.getErrorLoggingStream().println("<JNab configuration server> client connection failure");
		}
		catch (NullPointerException e2)
		{}
		continue;
	    }

	    // Reading command
	    BufferedReader br = null;
	    PrintStream ps = null;
	    try
	    {
		br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "US-ASCII"));
		ps = new PrintStream(socket.getOutputStream(), true, "US-ASCII");
	    }
	    catch (UnsupportedEncodingException e)
	    {
		// This exception can not occur since every platform should support ASCII encoding
	    }
	    catch (IOException e)
	    {
		try
		{
		    this.microServer.getDebugLoggingStream().println("<JNab configuration server> client communication failure");
		}
		catch (NullPointerException e2)
		{}

		try
		{
		    socket.close();
		}
		catch (IOException e2)
		{}
		continue;
	    }

	    ps.println("MicroServer configuration front-end, v1.0");
	    ps.println();
	    while (true)
	    {
		ps.print("> ");
		ps.flush();
		String cmd = null;
		try
		{
		    cmd = br.readLine();
		    if (cmd == null) throw new IOException();
		}
		catch (IOException e)
		{
		    break;
		}

		int connectionStatus = this.handleCommand(cmd.trim(), ps);
		if (connectionStatus == KEEP_ALIVE) continue;
		try
		{
		    socket.close();
		}
		catch (IOException e)
		{}
		if (connectionStatus == SERVER_SHUTDOWN)
		{
		    try
		    {
			this.serverSocket.close();
		    }
		    catch (IOException e)
		    {}
		    return;
		}

	    }

	}
    }

    /**
     * Internal method processing incoming commands.
     * 
     * @param cmd the incoming command to process.
     * @param ps the character stream where to write command processing output.
     * @return a connection state indication, either <tt>KEEP_ALIVE</tt>, <tt>CLIENT_SHUTDOWN</tt> or <tt>SERVER_SHUTDOWN</tt>.
     */
    private int handleCommand(String cmd, PrintStream ps)
    {
	try
	{
	    // Server shutdown command
	    if (cmd.equals("SHUTDOWN server"))
	    {
		return SERVER_SHUTDOWN;
	    }

	    // Client shutdown command
	    if (cmd.equals("SHUTDOWN client"))
	    {
		return CLIENT_SHUTDOWN;
	    }

	    if (cmd.equals("INFO server"))
	    {
		ps.println("Server bound to (" + this.microServer.getAddress() + "," + this.microServer.getPort() + ")");
		return KEEP_ALIVE;

	    }
	    else if (cmd.equals("INFO bunnies"))
	    {
		return this.handleInfoBunniesCommand(ps);
	    }
	    else if (cmd.startsWith("INFO bunny: "))
	    {
		return this.handleInfoBunnyCommand(cmd.substring(cmd.indexOf(':') + 1).trim(), ps);
	    }
	    else if (cmd.startsWith("SET bunny name: "))
	    {
		return this.handleSetBunnyNameCommand(cmd.substring(cmd.indexOf(':') + 1).trim(), ps);
	    }
	    else if (cmd.startsWith("SET bunny ping interval: "))
	    {
		return this.handleSetBunnyPingIntervalCommand(cmd.substring(cmd.indexOf(':') + 1).trim(), ps);
	    }
	    else if (cmd.startsWith("ADD bunny plugin: "))
	    {
		return this.handleAddBunnyPluginCommand(cmd.substring(cmd.indexOf(':') + 1).trim(), ps);
	    }
	    else if (cmd.startsWith("SET bunny plugin parameter: "))
	    {
		return this.handleSetBunnyPluginParameterCommand(cmd.substring(cmd.indexOf(':') + 1).trim(), ps);
	    }
	    else if (cmd.startsWith("REMOVE bunny plugin: "))
	    {
		return this.handleRemoveBunnyPluginCommand(cmd.substring(cmd.indexOf(':') + 1).trim(), ps);
	    }
	    else if (cmd.startsWith("SAVE bunny: "))
	    {
		return this.handleSaveBunnyCommand(cmd.substring(cmd.indexOf(':') + 1).trim(), ps);
	    }

	    else
	    {
		ps.println("KO (unsupported command)");
		return KEEP_ALIVE;
	    }
	}
	// Avoiding killing the server if ArrayIndexOutOfBounds or other runtime exception occurs
	catch (RuntimeException e)
	{
	    return KEEP_ALIVE;
	}

    }

    /**
     * Internal method processing "SAVE bunny:" command.
     * 
     * @param bunnySerial the serial number of the bunny to serialize.
     * @param ps the character stream where to write command processing output.
     * @return a connection state indication, either <tt>KEEP_ALIVE</tt>, <tt>CLIENT_SHUTDOWN</tt> or <tt>SERVER_SHUTDOWN</tt>.
     */
    private int handleSaveBunnyCommand(String bunnySerial, PrintStream ps)
    {
	Bunny bunny = null;
	try
	{
	    bunny = this.microServer.getBurrow().getBunny(bunnySerial);
	}
	catch (NoSuchBunnyException e)
	{
	    ps.println("KO (no such bunny)");
	    return KEEP_ALIVE;
	}

	try
	{
	    this.serializer.saveBunny(bunny);
	}
	catch (IOException e)
	{
	    ps.println("KO (serialization failure)");
	    return KEEP_ALIVE;
	}

	ps.println("OK");
	return KEEP_ALIVE;
    }

    /**
     * Internal method processing "REMOVE bunny plugin:" command.
     * 
     * @param cmdParameters the parameters of the command (i.e. the substring after ':').
     * @param ps the character stream where to write command processing output.
     * @return a connection state indication, either <tt>KEEP_ALIVE</tt>, <tt>CLIENT_SHUTDOWN</tt> or <tt>SERVER_SHUTDOWN</tt>.
     */
    private int handleRemoveBunnyPluginCommand(String cmdParameters, PrintStream ps)
    {
	int indexOfSpace = cmdParameters.indexOf(' ');
	if (indexOfSpace == -1)
	{
	    ps.println("KO (syntax error)");
	    return KEEP_ALIVE;
	}
	String bunnySerial = cmdParameters.substring(0, indexOfSpace).trim();
	String pluginName = cmdParameters.substring(indexOfSpace).trim();

	try
	{
	    Bunny bunny = this.microServer.getBurrow().getBunny(bunnySerial);
	    AbstractPlugin plugin = bunny.getPluginByName(pluginName);
	    bunny.removePlugin(plugin);
	    ps.println("OK");
	}
	catch (NoSuchBunnyException e)
	{
	    ps.println("KO (no such bunny)");
	}
	catch (NoSuchPluginException e)
	{
	    ps.println("KO (no such plugin)");
	}

	return KEEP_ALIVE;
    }

    /**
     * Internal method processing "SET bunny plugin parameter:" command.
     * 
     * @param cmdParameters the parameters of the command (i.e. the substring after ':').
     * @param ps the character stream where to write command processing output.
     * @return a connection state indication, either <tt>KEEP_ALIVE</tt>, <tt>CLIENT_SHUTDOWN</tt> or <tt>SERVER_SHUTDOWN</tt>.
     */
    private int handleSetBunnyPluginParameterCommand(String cmdParameters, PrintStream ps)
    {

	int indexOfSpace = cmdParameters.indexOf(' ');
	if (indexOfSpace == -1)
	{
	    ps.println("KO (syntax error)");
	    return KEEP_ALIVE;
	}
	String bunnySerial = cmdParameters.substring(0, indexOfSpace).trim();

	cmdParameters = cmdParameters.substring(indexOfSpace).trim();
	indexOfSpace = cmdParameters.indexOf(' ');
	if (indexOfSpace == -1)
	{
	    ps.println("KO (syntax error)");
	    return KEEP_ALIVE;
	}

	String pluginName = cmdParameters.substring(0, indexOfSpace).trim();

	cmdParameters = cmdParameters.substring(indexOfSpace).trim();
	indexOfSpace = cmdParameters.indexOf(' ');
	if (indexOfSpace == -1)
	{
	    ps.println("KO (syntax error)");
	    return KEEP_ALIVE;
	}
	String paramName = cmdParameters.substring(0, indexOfSpace).trim();

	String paramValue = cmdParameters.substring(indexOfSpace).trim();

	try
	{
	    Bunny bunny = this.microServer.getBurrow().getBunny(bunnySerial);
	    AbstractPlugin plugin = bunny.getPluginByName(pluginName);
	    if (plugin.setParameter(paramName, paramValue))
		ps.println("KO (no such parameter)");
	    else
		ps.println("OK");
	}
	catch (NoSuchBunnyException e)
	{
	    ps.println("KO (no such bunny)");
	}
	catch (NoSuchPluginException e)
	{
	    ps.println("KO (no such plugin)");
	}

	return KEEP_ALIVE;
    }

    /**
     * Internal method processing "ADD bunny plugin:" command.
     * 
     * @param cmdParameters the parameters of the command (i.e. the substring after ':').
     * @param ps the character stream where to write command processing output.
     * @return a connection state indication, either <tt>KEEP_ALIVE</tt>, <tt>CLIENT_SHUTDOWN</tt> or <tt>SERVER_SHUTDOWN</tt>.
     */
    private int handleAddBunnyPluginCommand(String cmdParameters, PrintStream ps)
    {
	int indexOfSpace = cmdParameters.indexOf(' ');
	if (indexOfSpace == -1)
	{
	    ps.println("KO (syntax error)");
	    return KEEP_ALIVE;
	}
	String bunnySerial = cmdParameters.substring(0, indexOfSpace).trim();
	String pluginName = cmdParameters.substring(indexOfSpace).trim();

	try
	{
	    Bunny bunny = this.microServer.getBurrow().getBunny(bunnySerial);
	    AbstractPlugin plugin = this.microServer.getPluginFactory().createPlugin(pluginName);
	    bunny.addPlugin(plugin);
	    ps.println("OK");
	}
	catch (NoSuchBunnyException e)
	{
	    ps.println("KO (no such bunny)");
	}
	catch (PluginCreationException e)
	{
	    ps.println("KO (plugin creation failure)");
	}

	return KEEP_ALIVE;
    }

    /**
     * Internal method processing "SET bunny ping interval:" command.
     * 
     * @param cmdParameters the parameters of the command (i.e. the substring after ':').
     * @param ps the character stream where to write command processing output.
     * @return a connection state indication, either <tt>KEEP_ALIVE</tt>, <tt>CLIENT_SHUTDOWN</tt> or <tt>SERVER_SHUTDOWN</tt>.
     */
    private int handleSetBunnyPingIntervalCommand(String cmdParameters, PrintStream ps)
    {
	int indexOfSpace = cmdParameters.indexOf(' ');
	if (indexOfSpace == -1)
	{
	    ps.println("KO (syntax error)");
	    return KEEP_ALIVE;
	}
	String bunnySerial = cmdParameters.substring(0, indexOfSpace).trim();

	int bunnyPing = 0;
	try
	{
	    bunnyPing = Integer.parseInt(cmdParameters.substring(indexOfSpace).trim());
	}
	catch (NumberFormatException e)
	{
	    ps.println("KO (syntax error)");
	    return KEEP_ALIVE;
	}
	try
	{
	    Bunny bunny = this.microServer.getBurrow().getBunny(bunnySerial);
	    bunny.setPingInterval(bunnyPing);
	    ps.println("OK");
	}
	catch (NoSuchBunnyException e)
	{
	    ps.println("KO (no such bunny)");
	}

	return KEEP_ALIVE;
    }

    /**
     * Internal method processing "SET bunny name:" command.
     * 
     * @param cmdParameters the parameters of the command (i.e. the substring after ':').
     * @param ps the character stream where to write command processing output.
     * @return a connection state indication, either <tt>KEEP_ALIVE</tt>, <tt>CLIENT_SHUTDOWN</tt> or <tt>SERVER_SHUTDOWN</tt>.
     */
    private int handleSetBunnyNameCommand(String cmdParameters, PrintStream ps)
    {
	int indexOfSpace = cmdParameters.indexOf(' ');
	if (indexOfSpace == -1)
	{
	    ps.println("KO (syntax error)");
	    return KEEP_ALIVE;
	}
	String bunnySerial = cmdParameters.substring(0, indexOfSpace).trim();
	String bunnyName = cmdParameters.substring(indexOfSpace).trim();

	try
	{
	    Bunny bunny = this.microServer.getBurrow().getBunny(bunnySerial);
	    bunny.setName(bunnyName);
	    ps.println("OK");
	}
	catch (NoSuchBunnyException e)
	{
	    ps.println("KO (no such bunny)");
	}

	return KEEP_ALIVE;
    }

    /**
     * Internal method processing "INFO bunny:" command.
     * 
     * @param cmdParameters the parameters of the command (i.e. the substring after ':').
     * @param ps the character stream where to write command processing output.
     * @return a connection state indication, either <tt>KEEP_ALIVE</tt>, <tt>CLIENT_SHUTDOWN</tt> or <tt>SERVER_SHUTDOWN</tt>.
     */
    private int handleInfoBunnyCommand(String cmdParameters, PrintStream ps)
    {
	try
	{
	    Bunny bunny = this.microServer.getBurrow().getBunny(cmdParameters);
	    ps.println("Name: " + bunny.getName());
	    ps.println("Ping interval: " + bunny.getPingInterval());
	    Set<AbstractPlugin> plugins = bunny.getPlugins();
	    ps.println("" + plugins.size() + " plugins: ");

	    for (AbstractPlugin plugin : plugins)
	    {
		ps.println(plugin.getName());
	    }
	}
	catch (NoSuchBunnyException e)
	{
	    ps.println("KO (no such bunny)");
	}

	return KEEP_ALIVE;
    }

    /**
     * Internal method processing "INFO bunnies" command.
     * 
     * @param ps the character stream where to write command processing output.
     * @return a connection state indication, either <tt>KEEP_ALIVE</tt>, <tt>CLIENT_SHUTDOWN</tt> or <tt>SERVER_SHUTDOWN</tt>.
     */
    private int handleInfoBunniesCommand(PrintStream ps)
    {
	Burrow burrow = this.microServer.getBurrow();
	Collection<Bunny> bunnies = burrow.getBunnies();
	ps.println("" + bunnies.size() + " bunnies currently in burrow :");
	ps.println();
	for (Bunny bunny : bunnies)
	{
	    ps.println(bunny.getSerialNumber());
	}
	return KEEP_ALIVE;
    }

}
