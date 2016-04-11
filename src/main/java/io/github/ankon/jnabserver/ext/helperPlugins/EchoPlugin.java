package io.github.ankon.jnabserver.ext.helperPlugins;

import java.io.FileOutputStream;
import java.io.IOException;

import io.github.ankon.jnabserver.core.events.RecordEventListener;
import io.github.ankon.jnabserver.core.plugins.AbstractPlugin;
import io.github.ankon.jnabserver.core.protocol.MessageBlock;
import io.github.ankon.jnabserver.core.protocol.Packet;
import io.github.ankon.jnabserver.core.protocol.PingIntervalBlock;

/**
 * Plugin echoing on a bunny the sound that has been previously recorded (on the same bunny).
 * 
 * @author Juha-Pekka Rajaniemi
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class EchoPlugin extends AbstractPlugin implements RecordEventListener
{
    /**
     * Plugin name.
     */
    private static String PLUGIN_NAME = "Echo_Plugin";

    /**
     * Set of parameters supported by the plugin;
     */
    private static String[] PARAMETERS = {};

    /**
     * Creating a new Echo plugin instance.
     */
    public EchoPlugin()
    {
	super(PLUGIN_NAME, PARAMETERS);
    }

    /**
     * Echoing the recorded voice.
     * 
     * @see RecordEventListener#onSimpleRecord(byte[])
     */
    public void onSimpleRecord(byte[] data)
    {
	// Writing data to echo-<MAC>.wav
	try
	{
	    FileOutputStream fos = new FileOutputStream("echo" + this.bunny.getSerialNumber() + ".wav");
	    for (int element : data)
		fos.write(element);
	    fos.close();
	}
	catch (IOException e)
	{
	    return;
	}

	// Playing recorded sound
	Packet p = new Packet();
	MessageBlock mb = new MessageBlock(600);
	mb.addPlayLocalSoundCommand("echo" + this.bunny.getSerialNumber() + ".wav");
	mb.addWaitPreviousEndCommand();
	p.addBlock(mb);
	p.addBlock(new PingIntervalBlock(1));
	this.bunny.addPacket(p);
    }

    /**
     * @see io.github.ankon.jnabserver.core.events.RecordEventListener#onDoubleRecord(byte[])
     */
    public void onDoubleRecord(byte[] data)
    {}

}
