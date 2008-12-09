package jNab.ext.helperPlugins;

import jNab.core.events.ClickEventListener;
import jNab.core.events.EarsEventListener;
import jNab.core.events.PingEventListener;
import jNab.core.events.RFIDEventListener;
import jNab.core.events.RecordEventListener;
import jNab.core.events.StopEventListener;
import jNab.core.plugins.AbstractPlugin;

/**
 * Plugin capturing every event but doing nothing !
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class NullPlugin extends AbstractPlugin implements ClickEventListener, EarsEventListener, PingEventListener, RecordEventListener,
	RFIDEventListener, StopEventListener
{

    /**
     * Plugin name.
     */
    private static String PLUGIN_NAME = "Null_Plugin";

    /**
     * Set of parameters supported by the plugin;
     */
    private static String[] PARAMETERS = {};

    /**
     * Creating a new Null plugin instance.
     */
    public NullPlugin()
    {
	super(PLUGIN_NAME, PARAMETERS);
    }

    /**
     * @see jNab.core.events.ClickEventListener#onDoubleClick()
     */
    public void onDoubleClick()
    {}

    /**
     * @see jNab.core.events.ClickEventListener#onSingleClick()
     */
    public void onSingleClick()
    {}

    /**
     * @see jNab.core.events.EarsEventListener#onEarsMove(int, int)
     */
    public void onEarsMove(int rightEar, int leftEar)
    {}

    /**
     * @see jNab.core.events.RecordEventListener#onSimpleRecord(byte[])
     */
    public void onSimpleRecord(byte[] data)
    {}

    /**
     * @see jNab.core.events.RFIDEventListener#onRfid(java.lang.String)
     */
    public void onRfid(String rfid)
    {}

    /**
     * @see jNab.core.events.StopEventListener#onEndOfMessage()
     */
    public void onEndOfMessage()
    {}

    /**
     * @see jNab.core.events.StopEventListener#onSingleClickWhilePlaying()
     */
    public void onSingleClickWhilePlaying()
    {}

    /**
     * @see jNab.core.events.RecordEventListener#onDoubleRecord(byte[])
     */
    public void onDoubleRecord(byte[] data)
    {}

    /**
     * @see jNab.core.events.PingEventListener#onPing()
     */
    public void onPing()
    {}

}
