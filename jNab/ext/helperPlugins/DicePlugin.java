package jNab.ext.helperPlugins;

import jNab.core.events.ClickEventListener;
import jNab.core.plugins.AbstractPlugin;
import jNab.core.protocol.MessageBlock;
import jNab.core.protocol.Packet;
import jNab.core.protocol.PingIntervalBlock;

import java.util.Random;

/**
 * Plugin which makes the bunny rolling dices!
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class DicePlugin extends AbstractPlugin implements ClickEventListener
{
    /**
     * Plugin name.
     */
    private static String PLUGIN_NAME = "Dice_Plugin";

    /**
     * Set of parameters supported by the plugin;
     */
    private static String[] PARAMETERS = {};

    /**
     * Creating a new Dice plugin instance.
     * 
     */
    public DicePlugin()
    {
	super(PLUGIN_NAME, PARAMETERS);
    }

    /**
     * Rolling one dice.
     * 
     * @see jNab.core.events.ClickEventListener#onSingleClick()
     */
    public void onSingleClick()
    {
	Packet p = new Packet();

	// Generating a random number between 1 and 6
	Random rd = new Random();
	int n = rd.nextInt(6) + 1;

	// Playing the associated sound files
	MessageBlock mb = new MessageBlock(333);
	mb.addPlayLocalSoundCommand("files/sounds/dice/get.mp3");
	mb.addWaitPreviousEndCommand();
	mb.addPlayLocalSoundCommand("files/sounds/dice/" + n + ".mp3");
	mb.addWaitPreviousEndCommand();
	p.addBlock(mb);
	p.addBlock(new PingIntervalBlock(1));
	this.bunny.addPacket(p);
    }

    /**
     * Rolling two dices.
     * 
     * @see jNab.core.events.ClickEventListener#onDoubleClick()
     */
    public void onDoubleClick()
    {
	// Generating two random numbers between 1 and 6
	Random rd = new Random();
	int n1 = rd.nextInt(6) + 1;
	int n2 = rd.nextInt(6) + 1;

	Packet p = new Packet();

	// Playing the associated sound on the bunny
	MessageBlock mb = new MessageBlock(333);
	mb.addPlaySoundCommand("broadcast/files/sounds/dice/get.mp3");
	mb.addWaitPreviousEndCommand();
	mb.addPlaySoundCommand("broadcast/files/sounds/dice/" + n1 + ".mp3");
	mb.addWaitPreviousEndCommand();
	mb.addPlaySoundCommand("broadcast/files/sounds/dice/" + n2 + ".mp3");
	mb.addWaitPreviousEndCommand();
	p.addBlock(mb);
	p.addBlock(new PingIntervalBlock(1));
	this.bunny.addPacket(p);
    }
}
