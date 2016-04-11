package io.github.ankon.jnabserver.ext.helperPlugins;

import io.github.ankon.jnabserver.core.events.ClickEventListener;
import io.github.ankon.jnabserver.core.events.EarsEventListener;
import io.github.ankon.jnabserver.core.events.PingEventListener;
import io.github.ankon.jnabserver.core.events.RFIDEventListener;
import io.github.ankon.jnabserver.core.events.RecordEventListener;
import io.github.ankon.jnabserver.core.events.StopEventListener;
import io.github.ankon.jnabserver.core.plugins.AbstractPlugin;

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
     * @see io.github.ankon.jnabserver.core.events.ClickEventListener#onDoubleClick()
     */
    public void onDoubleClick()
    {}

    /**
     * @see io.github.ankon.jnabserver.core.events.ClickEventListener#onSingleClick()
     */
    public void onSingleClick()
    {}

    /**
     * @see io.github.ankon.jnabserver.core.events.EarsEventListener#onEarsMove(int, int)
     */
    public void onEarsMove(int rightEar, int leftEar)
    {}

    /**
     * @see io.github.ankon.jnabserver.core.events.RecordEventListener#onSimpleRecord(byte[])
     */
    public void onSimpleRecord(byte[] data)
    {}

    /**
     * @see io.github.ankon.jnabserver.core.events.RFIDEventListener#onRfid(java.lang.String)
     */
    public void onRfid(String rfid)
    {}

    /**
     * @see io.github.ankon.jnabserver.core.events.StopEventListener#onEndOfMessage()
     */
    public void onEndOfMessage()
    {}

    /**
     * @see io.github.ankon.jnabserver.core.events.StopEventListener#onSingleClickWhilePlaying()
     */
    public void onSingleClickWhilePlaying()
    {}

    /**
     * @see io.github.ankon.jnabserver.core.events.RecordEventListener#onDoubleRecord(byte[])
     */
    public void onDoubleRecord(byte[] data)
    {}

    /**
     * @see io.github.ankon.jnabserver.core.events.PingEventListener#onPing()
     */
    public void onPing()
    {}

}
