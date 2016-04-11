package io.github.ankon.jnabserver.ext.helperPlugins;

import io.github.ankon.jnabserver.core.events.ClickEventListener;
import io.github.ankon.jnabserver.core.events.EarsEventListener;
import io.github.ankon.jnabserver.core.events.PingEventListener;
import io.github.ankon.jnabserver.core.events.RFIDEventListener;
import io.github.ankon.jnabserver.core.events.RecordEventListener;
import io.github.ankon.jnabserver.core.events.StopEventListener;
import io.github.ankon.jnabserver.core.plugins.AbstractPlugin;

/**
 * Plugin capturing every event and just logging them on standard output
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class SystemOutLoggerPlugin extends AbstractPlugin implements ClickEventListener, EarsEventListener, PingEventListener,
	RecordEventListener, RFIDEventListener, StopEventListener
{

    /**
     * Plugin name.
     */
    private static String PLUGIN_NAME = "SystemOutLogger_Plugin";

    /**
     * Set of parameters supported by the plugin;
     */
    private static String[] PARAMETERS = {};

    /**
     * Creating a new Null plugin instance.
     */
    public SystemOutLoggerPlugin()
    {
	super(PLUGIN_NAME, PARAMETERS);
    }

    /**
     * @see io.github.ankon.jnabserver.core.events.ClickEventListener#onDoubleClick()
     */
    public void onDoubleClick()
    {
	System.out.println("<SystemOutLoggerPlugin> double-click event received");
    }

    /**
     * @see io.github.ankon.jnabserver.core.events.ClickEventListener#onSingleClick()
     */
    public void onSingleClick()
    {
	System.out.println("<SystemOutLoggerPlugin> single-click event received");
    }

    /**
     * @see io.github.ankon.jnabserver.core.events.EarsEventListener#onEarsMove(int, int)
     */
    public void onEarsMove(int rightEar, int leftEar)
    {
	System.out.println("<SystemOutLoggerPlugin> ears move event received (Left=" + leftEar + ", Right=" + rightEar + ")");
    }

    /**
     * @see io.github.ankon.jnabserver.core.events.RecordEventListener#onSimpleRecord(byte[])
     */
    public void onSimpleRecord(byte[] data)
    {
	System.out.println("<SystemOutLoggerPlugin> single-click record event received");
    }

    /**
     * @see io.github.ankon.jnabserver.core.events.RecordEventListener#onDoubleRecord(byte[])
     */
    public void onDoubleRecord(byte[] data)
    {
	System.out.println("<SystemOutLoggerPlugin> double-click record event received");
    }

    /**
     * @see io.github.ankon.jnabserver.core.events.RFIDEventListener#onRfid(java.lang.String)
     */
    public void onRfid(String rfid)
    {
	System.out.println("<SystemOutLoggerPlugin> RFID event received (tagID=" + rfid + ")");
    }

    /**
     * @see io.github.ankon.jnabserver.core.events.StopEventListener#onEndOfMessage()
     */
    public void onEndOfMessage()
    {
	System.out.println("<SystemOutLoggerPlugin> end-of-message event received");
    }

    /**
     * @see io.github.ankon.jnabserver.core.events.StopEventListener#onSingleClickWhilePlaying()
     */
    public void onSingleClickWhilePlaying()
    {
	System.out.println("<SystemOutLoggerPlugin> single-click while playing event received");
    }

    /**
     * @see io.github.ankon.jnabserver.core.events.PingEventListener#onPing()
     */
    public void onPing()
    {
	System.out.println("<SystemOutLoggerPlugin> ping event received");
    }

}
