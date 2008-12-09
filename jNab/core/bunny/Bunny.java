package jNab.core.bunny;

import jNab.core.events.ClickEventListener;
import jNab.core.events.EarsEventListener;
import jNab.core.events.PingEventListener;
import jNab.core.events.RFIDEventListener;
import jNab.core.events.RecordEventListener;
import jNab.core.events.StopEventListener;
import jNab.core.exceptions.NoSuchPluginException;
import jNab.core.plugins.AbstractPlugin;
import jNab.core.protocol.HTTPRequest;
import jNab.core.protocol.MessageBlock;
import jNab.core.protocol.Packet;
import jNab.core.protocol.PingIntervalBlock;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Wrapping class for bunnies.
 *
 * @author Juha-Pekka Rajaniemi
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class Bunny
{
    /**
     * Default name for the bunny.
     */
    private final static String DEFAULT_NAME = "Bugs";

    /**
     * Default delay, in seconds, between ping requests of the bunny.
     */
    private final static int DEFAULT_PING_INTERVAL = 20;

    /**
     * Event type for simple ping event.
     */
    public final static int SIMPLE_PING_EVENT = 0;

    /**
     * Event type for double click event.
     */
    public final static int DOUBLE_CLICK_EVENT = 1;

    /**
     * Event type for end of message event.
     */
    public final static int END_OF_MESSAGE_EVENT = 2;

    /**
     * Event type for single click event.
     */
    public final static int SINGLE_CLICK_EVENT = 3;

    /**
     * Event type for single click while playing event.
     */
    public final static int STOP_EVENT = 5;

    /**
     * Event ytpe for ears movement event.
     */
    public final static int EARS_MOVE_EVENT = 8;

    /**
     * ID to send to bunnies to activate them.
     */
    private static final String ID_ACTIVATE = "7FFFFFFF";

    /**
     * ID to send to bunnies to tell them to sleep.
     */
    private static final String ID_SLEEP = "7FFFFFFE";

    /**
     * Set of all plugins attached to the bunny.
     */
    private Set<AbstractPlugin> allPlugins;

    /**
     * Set of plugins, attached to the bunny, able to process "ping" events.
     */
    private Set<AbstractPlugin> pingEventPlugins;

    /**
     * Set of plugins, attached to the bunny, able to process "click" events.
     */
    private Set<AbstractPlugin> clickEventPlugins;

    /**
     * Set of plugins, attached to the bunny, able to process "ears movement" events.
     */
    private Set<AbstractPlugin> earsEventPlugins;

    /**
     * Set of plugins, attached to the bunny, able to process "stop" events.
     */
    private Set<AbstractPlugin> stopEventPlugins;

    /**
     * Set of plugins, attached to the bunny, able to process "record" events.
     */
    private Set<AbstractPlugin> recordEventPlugins;

    /**
     * Set of plugins, attached to the bunny, able to process "RFID" events.
     */
    private Set<AbstractPlugin> RFIDEventPlugins;

    /**
     * Serial number of the bunny.
     */
    private String serialNumber;

    /**
     * Name of the bunny.
     */
    private String name;

    /**
     * Burrow where the bunny is stored.
     */
    private Burrow burrow;

    /**
     * Status of the connection between bunny and server.
     */
    private boolean connectionStatus;

    /**
     * ID of the last message played by the bunny.
     */
    private String lastPlayedMessage;

    /**
     * Delay, in seconds, between each ping request of the bunny.
     */
    private int pingInterval;

    /**
     * FIFO list of packets to be sent to the bunny
     */
    private List<Packet> packetsToSend;

    /**
     * Creating a new bunny instance, using a given serial number.
     *
     * @param serialNumber The MAC address of the NabazTag/Tag.
     */
    public Bunny(String serialNumber)
    {
	// Initializing name and serial number
	this.name = Bunny.DEFAULT_NAME;
	this.serialNumber = serialNumber.toLowerCase();

	// Initializing burrow
	// N.B. the bunny is initially outside any burrow
	this.burrow = null;

	// Initializing packets and plugins collections
	this.packetsToSend = Collections.synchronizedList(new LinkedList<Packet>());

	this.allPlugins = Collections.synchronizedSet(new HashSet<AbstractPlugin>());
	this.clickEventPlugins = Collections.synchronizedSet(new HashSet<AbstractPlugin>());
	this.pingEventPlugins = Collections.synchronizedSet(new HashSet<AbstractPlugin>());
	this.earsEventPlugins = Collections.synchronizedSet(new HashSet<AbstractPlugin>());
	this.stopEventPlugins = Collections.synchronizedSet(new HashSet<AbstractPlugin>());
	this.recordEventPlugins = Collections.synchronizedSet(new HashSet<AbstractPlugin>());
	this.RFIDEventPlugins = Collections.synchronizedSet(new HashSet<AbstractPlugin>());

	// Setting the bunny to the initial state w.r.t. network protocol
	this.lastPlayedMessage = "0";
	this.connectionStatus = true;
	this.pingInterval = DEFAULT_PING_INTERVAL;
    }

    /**
     * Setting the name of the bunny.
     *
     * @param name the name of the bunny.
     */
    public void setName(String name)
    {
	this.name = name;
    }

    /**
     * Getting the name of the bunny.
     *
     * @return the name of the bunny.
     */
    public String getName()
    {
	return this.name;
    }

    /**
     * Getting the serial number of the bunny.
     *
     * @return the serial number of the bunny.
     */
    public String getSerialNumber()
    {
	return this.serialNumber;
    }

    /**
     * Setting the burrow where the bunny is stored.
     *
     * @param burrow the burrow where the bunny is stored.
     */
    public void setBurrow(Burrow burrow)
    {
	this.burrow = burrow;
    }

    /**
     * Getting the burrow where the bunny is stored.
     *
     * @return the burrow where the bunny is stored.
     */
    public Burrow getBurrow()
    {
	return this.burrow;
    }

    /**
     * Getting the set of plugins belonging to the bunny.
     *
     * @return the set of plugins belonging to the bunny.
     */
    public Set<AbstractPlugin> getPlugins()
    {
	return this.allPlugins;
    }

    /**
     * Getting a plugin belonging to the bunny, given its name.
     *
     * @param pluginName the name of the plugin to search for.
     * @return the plugin belonging to the bunny and whose name is <tt>pluginName</tt>.
     * @throws NoSuchPluginException if no plugin whose name is <tt>pluginName</tt> belongs to the bunny.
     */
    public AbstractPlugin getPluginByName(String pluginName) throws NoSuchPluginException
    {
	for (AbstractPlugin plugin : this.allPlugins)
	{
	    if (plugin.getName().equals(pluginName)) return plugin;
	}

	throw new NoSuchPluginException();
    }

    /**
     * Unregistering a plugin handling events coming from the bunny.
     *
     * @param plugin the name of the plugin to remove.
     */
    public synchronized void removePlugin(AbstractPlugin plugin)
    {
	if (this.allPlugins.remove(plugin))
	{
	    for (Class<?> i : plugin.getClass().getInterfaces())
	    {
		if (i.getName().equals("jNab.core.events.ClickEventListener"))
		    this.clickEventPlugins.remove(plugin);
		else if (i.getName().equals("jNab.core.events.PingEventListener"))
		    this.pingEventPlugins.remove(plugin);
		else if (i.getName().equals("jNab.core.events.EarsEventListener"))
		    this.earsEventPlugins.remove(plugin);
		else if (i.getName().equals("jNab.core.events.StopEventListener"))
		    this.stopEventPlugins.remove(plugin);
		else if (i.getName().equals("jNab.core.events.RecordEventListener"))
		    this.recordEventPlugins.remove(plugin);
		else if (i.getName().equals("jNab.core.events.RFIDEventListener")) this.RFIDEventPlugins.remove(plugin);
	    }
	}
	plugin.setBunny(null);
    }

    /**
     * Registering a new plugin handling events coming from the bunny.
     *
     * @param plugin the name of the plugin to add.
     */
    public synchronized void addPlugin(AbstractPlugin plugin)
    {
	if (this.allPlugins.add(plugin))
	{
	    for (Class<?> i : plugin.getClass().getInterfaces())
	    {
		if (i.getName().equals("jNab.core.events.ClickEventListener"))
		    this.clickEventPlugins.add(plugin);
		else if (i.getName().equals("jNab.core.events.PingEventListener"))
		    this.pingEventPlugins.add(plugin);
		else if (i.getName().equals("jNab.core.events.EarsEventListener"))
		    this.earsEventPlugins.add(plugin);
		else if (i.getName().equals("jNab.core.events.StopEventListener"))
		    this.stopEventPlugins.add(plugin);
		else if (i.getName().equals("jNab.core.events.RecordEventListener"))
		    this.recordEventPlugins.add(plugin);
		else if (i.getName().equals("jNab.core.events.RFIDEventListener")) this.RFIDEventPlugins.add(plugin);
	    }
	}
	plugin.setBunny(this);
    }

    /**
     * Getting the connection status of the bunny.
     *
     * @return <tt>true</tt> if the bunny is connected, <tt>false</tt> if not.
     */
    public boolean getConnectionStatus()
    {
	return this.connectionStatus;
    }

    /**
     * Setting the connection status of the bunny.
     *
     * @param status the connection status of the bunny (<tt>true</tt> if the bunny is connected, <tt>false</tt> if not).
     */
    public void setConnectionStatus(boolean status)
    {
	this.connectionStatus = status;
    }

    /**
     * Setting the delay, in seconds, between ping requests of the bunny.
     *
     * @param interval the delay, in seconds, between ping requests of the bunny.
     */
    public void setPingInterval(int interval)
    {
	this.pingInterval = interval;
    }

    /**
     * Getting the delay, in seconds, between ping requests of the bunny.
     *
     * @return the delay, in seconds, between ping requests of the bunny.
     */
    public int getPingInterval()
    {
	return this.pingInterval;
    }

    /**
     * Getting the ID of the last message played by the bunny.
     *
     * @return the ID of the last message played by the bunny, as a string.
     */
    public String getLastPlayedMessageID()
    {
	return this.lastPlayedMessage;
    }

    /**
     * Getting the activity state of the bunny.
     *
     * @return <tt>true</tt> if the bunny is awaken, <tt>false</tt> if it is sleeping.
     */
    public boolean isAwaken()
    {
	return !this.lastPlayedMessage.equals(ID_SLEEP);
    }

    /**
     * Adding a new packet to the list of packets to be sent to the bunny.
     *
     * @param packet the packet to add.
     */
    public void addPacket(Packet packet)
    {
	// Adding a ping interval block if there is no one in the list of packets
	if (!packet.isPingBlockPresent()) packet.addBlock(new PingIntervalBlock(this.pingInterval));

	this.packetsToSend.add(packet);
    }

    /**
     * Forcing a packet to be sent before all others.
     *
     * @param packet the packet to force.
     */
    public void forcePacket(Packet packet)
    {
	this.packetsToSend.add(0, packet);
    }

    /**
     * Getting the next packet to be sent to the bunny, removing it from the list of packets to be sent. If there is no packets in the list,
     * <tt>null</tt> is returned.
     *
     * @return the next packet to be sent to bunny.
     */
    public Packet getNextPacket()
    {
	try
	{
	    return this.packetsToSend.remove(0);
	}
	catch (IndexOutOfBoundsException e)
	{
	    return null;
	}
    }

    /**
     * Notifying the bunny to wake up.
     */
    public void wakeUp()
    {
	// Sending a new packet notifying the bunny to wake up,
	// and asking the bunny to ping immediately after
	Packet p = new Packet();
	MessageBlock mb = new MessageBlock(Integer.parseInt(ID_ACTIVATE, 16));
	p.addBlock(mb);
	p.addBlock(new PingIntervalBlock(1));
	forcePacket(p);
    }

    /**
     * Notifying the bunny to go to sleep.
     */
    public void goToSleep()
    {
	// Sending a new packet notifying the bunny to go to sleep
	Packet p = new Packet();
	MessageBlock mb = new MessageBlock(Integer.parseInt(ID_SLEEP, 16));
	p.addBlock(mb);
	forcePacket(p);
    }

    /**
     * Handling a new event coming from the bunny by notifying each relevant plugin.
     *
     * @param eventType the event type (should be one of the <tt><i>XY</i>_EVENT</tt> values, as defined by constant fields.
     * @param eventParams information that come with the event if relevant (e.g. ears positions in case of ears movement event), as an array
     *            of Object references.
     */
    public void handleEvent(int eventType, Object[] eventParams)
    {
	switch (eventType)
	{
	case SIMPLE_PING_EVENT: // Simple ping, notifying that it is still connected
	    try
	    {
		this.getBurrow().getMicroServer().getDebugLoggingStream()
			.println("<bunny/" + this.serialNumber + "> Handling a ping event");
	    }
	    catch (NullPointerException e)
	    {}

	    // TODO is connection status really necessary ?
	    this.connectionStatus = true;

	    if (!this.packetsToSend.isEmpty())
	    {
		// Until the list of packets to send is empty, no plugin is called and the packets ar sent
		// one by one forcing the bunny to re-ping each immediately after
		try
		{
		    this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			    "<bunny/" + this.serialNumber + "> Draining outgoing packets");
		}
		catch (NullPointerException e)
		{}
		Packet packet = this.getNextPacket();
		packet.setPingIntervalBlock(1);
		this.forcePacket(packet);
	    }
	    else
	    {
		// List of packets to send is empty
		// So, the event has to be notified to registered plugins
		for (AbstractPlugin plugin : this.pingEventPlugins)
		{
		    try
		    {
			this.getBurrow().getMicroServer().getDebugLoggingStream().println(
				"<bunny/" + this.serialNumber + "> Calling " + plugin.getName() + " plugin");
		    }
		    catch (NullPointerException e)
		    {}
		    ((PingEventListener) plugin).onPing();
		}
	    }
	    return;

	case SINGLE_CLICK_EVENT: // Single click
	    try
	    {
		this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			"<bunny/" + this.serialNumber + "> Handling a single-click event");
	    }
	    catch (NullPointerException e)
	    {}
	    for (AbstractPlugin plugin : this.clickEventPlugins)
	    {
		try
		{
		    this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			    "<bunny/" + this.serialNumber + "> Calling " + plugin.getName() + " plugin");
		}
		catch (NullPointerException e)
		{}
		((ClickEventListener) plugin).onSingleClick();
	    }
	    return;

	case DOUBLE_CLICK_EVENT: // Double click
	    try
	    {
		this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			"<bunny/" + this.serialNumber + "> Handling a double-click event");
	    }
	    catch (NullPointerException e)
	    {}
	    for (AbstractPlugin plugin : this.clickEventPlugins)
	    {
		try
		{
		    this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			    "<bunny/" + this.serialNumber + "> Calling " + plugin.getName() + " plugin");
		}
		catch (NullPointerException e)
		{}
		((ClickEventListener) plugin).onDoubleClick();
	    }
	    return;

	case STOP_EVENT: // Single click while playing
	    try
	    {
		this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			"<bunny/" + this.serialNumber + "> Handling a single-click while playing event");
	    }
	    catch (NullPointerException e)
	    {}
	    for (AbstractPlugin plugin : this.stopEventPlugins)
	    {
		try
		{
		    this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			    "<bunny/" + this.serialNumber + "> Calling " + plugin.getName() + " plugin");
		}
		catch (NullPointerException e)
		{}
		((StopEventListener) plugin).onSingleClickWhilePlaying();
	    }
	    return;

	case END_OF_MESSAGE_EVENT: // End of message playing
	    try
	    {
		this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			"<bunny/" + this.serialNumber + "> Handling an end-of-message event");
	    }
	    catch (NullPointerException e)
	    {}
	    for (AbstractPlugin plugin : this.stopEventPlugins)
	    {
		try
		{
		    this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			    "<bunny/" + this.serialNumber + "> Calling " + plugin.getName() + " plugin");
		}
		catch (NullPointerException e)
		{}
		((StopEventListener) plugin).onEndOfMessage();
	    }
	    return;

	case EARS_MOVE_EVENT: // Ears move
	    int rightEar = (Integer) eventParams[0];
	    int leftEar = (Integer) eventParams[1];
	    try
	    {
		this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			"<bunny/" + this.serialNumber + "> Handling an ears move event (Left=" + leftEar + ", Right=" + rightEar + ")");
	    }
	    catch (NullPointerException e)
	    {}
	    for (AbstractPlugin plugin : this.earsEventPlugins)
	    {
		try
		{
		    this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			    "<bunny/" + this.serialNumber + "> Calling " + plugin.getName() + " plugin");
		}
		catch (NullPointerException e)
		{}
		((EarsEventListener) plugin).onEarsMove(rightEar, leftEar);
	    }
	default:
	    try
	    {
		this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			"<bunny/" + this.serialNumber + "> Ignoring an unknown event");
	    }
	    catch (NullPointerException e)
	    {}
	    return;
	}
    }

    /**
     * Handling a new request coming from the bunny and dispatching it to the attached plugins.
     *
     * @param request the request coming from the bunny.
     */
    public void handleRequest(HTTPRequest request)
    {
	// Refreshing the connection status
	this.connectionStatus = true;

	// Updating the ID of the last played message
	if (request.getURLParam("requestfile").equals("/vl/p4.jsp")) this.lastPlayedMessage = request.getURLParam("tc");

	// Waking up the bunny if needed
	if (this.lastPlayedMessage.equals("0")) wakeUp();

	// Redirecting the current request to the relevant plugin

	// Case 1: ping request
	if (request.getURLParam("requestfile").equals("/vl/p4.jsp"))
	{
	    try
	    {
		int eventType = Integer.parseInt(request.getURLParam("sd").substring(0, 1));
		if (eventType == EARS_MOVE_EVENT)
		{
		    Integer[] eventParams = { Integer.parseInt(request.getURLParam("sd").charAt(1) + "", 16),
			    Integer.parseInt(request.getURLParam("sd").substring(2), 16) };
		    this.handleEvent(eventType, eventParams);
		}
		else
		    this.handleEvent(eventType, null);
	    }
	    catch (NumberFormatException e)
	    {}
	}

	// Case 2: RFID request
	else if (request.getURLParam("requestfile").equals("/vl/rfid.jsp"))
	{
	    String tagId = request.getURLParam("t");
	    try
	    {
		this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			"<bunny/" + this.serialNumber + "> Handling a RFID event (tagID = " + tagId + ")");
	    }
	    catch (NullPointerException e)
	    {}
	    for (AbstractPlugin plugin : this.RFIDEventPlugins)
	    {
		try
		{
		    this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			    "<bunny/" + this.serialNumber + "> Calling " + plugin.getName() + " plugin");
		}
		catch (NullPointerException e)
		{}
		((RFIDEventListener) plugin).onRfid(tagId);
	    }
	}

	// Case 3: Record request, simple click
	else if (request.getURLParam("requestfile").equals("/vl/record.jsp") && request.getURLParam("m").equals("0"))
	{
	    try
	    {
		this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			"<bunny/" + this.serialNumber + "> Handling a single-click record event");
	    }
	    catch (NullPointerException e)
	    {}
	    for (AbstractPlugin plugin : this.recordEventPlugins)
	    {
		try
		{
		    this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			    "<bunny/" + this.serialNumber + "> Calling " + plugin.getName() + " plugin");
		}
		catch (NullPointerException e)
		{}
		((RecordEventListener) plugin).onSimpleRecord(request.getPostData());
	    }
	}

	// Case 4: Record request, double click
	else if (request.getURLParam("requestfile").equals("/vl/record.jsp") && request.getURLParam("m").equals("1"))
	{
	    try
	    {
		this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			"<bunny/" + this.serialNumber + "> Handling a double-click record event");
	    }
	    catch (NullPointerException e)
	    {}
	    for (AbstractPlugin plugin : this.recordEventPlugins)
	    {
		try
		{
		    this.getBurrow().getMicroServer().getDebugLoggingStream().println(
			    "<bunny/" + this.serialNumber + "> Calling " + plugin.getName() + " plugin");
		}
		catch (NullPointerException e)
		{}
		((RecordEventListener) plugin).onDoubleRecord(request.getPostData());
	    }
	}
    }

    /**
     * N.B. bunnies are equal if their serial numbers are the same.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object toCompare)
    {
	Bunny bunny = null;
	try
	{
	    bunny = (Bunny) toCompare;
	}
	catch (ClassCastException e)
	{
	    return false;
	}
	return (bunny.getSerialNumber().equals(this.serialNumber));
    }
}
