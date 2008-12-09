package jNab.core.bunny;

import jNab.core.exceptions.NoSuchBunnyException;
import jNab.core.server.MicroServer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for bunnies.
 * 
 * @author Juha-Pekka Rajaniemi
 * @author Ville Antila
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */

public class Burrow
{
    /**
     * Map indexing bunnies, whose key is a bunny serial number and whose value is a NabazTag/Tag wrapping object.
     */
    private Map<String, Bunny> bunnies;

    /**
     * Micro server hosting the burrow.
     */
    private MicroServer microServer;

    /**
     * Creating a new burrow instance.
     * 
     */
    public Burrow()
    {
	// Creating a empty synchronized bunnies map
	this.bunnies = Collections.synchronizedMap(new HashMap<String, Bunny>());

	// Initially, the burrow is not part of any micro server
	this.microServer = null;
    }

    /**
     * Setting the micro server hosting the burrow.
     * 
     * @param microServer the micro server hosting the burrow.
     */
    public void setMicroServer(MicroServer microServer)
    {
	this.microServer = microServer;
    }

    /**
     * Getting the micro server hosting the burrow.
     * 
     * @return the micro server hosting the burrow.
     */
    public MicroServer getMicroServer()
    {
	return this.microServer;
    }

    /**
     * Adding a bunny to the burrow.
     * 
     * @param bunny the bunny to add.
     */
    public void addBunny(Bunny bunny)
    {
	this.bunnies.put(bunny.getSerialNumber(), bunny);
	bunny.setBurrow(this);
    }

    /**
     * Adding a bunny to the burrow.
     * 
     * @param serialNumber the serial number of the bunny to add.
     */
    public void addBunny(String serialNumber)
    {
	this.addBunny(new Bunny(serialNumber));
    }

    /**
     * Removing a bunny from the burrow.
     * 
     * @param serialNumber the serial number of the bunny to remove.
     * @throws NoSuchBunnyException if there is no bunny whose serial number is <tt>serialNumber</tt> in the burrow.
     */
    public void removeBunny(String serialNumber) throws NoSuchBunnyException
    {
	Bunny bunny = this.bunnies.remove(serialNumber);
	if (bunny == null) throw new NoSuchBunnyException();
	bunny.setBurrow(null);
    }

    /**
     * Getting the collection of bunnies currently in the burrow.
     * 
     * @return the collection of bunnies currently in the burrow.
     */
    public Collection<Bunny> getBunnies()
    {
	return this.bunnies.values();
    }

    /**
     * Getting a bunny from the burrow, given its serial number.
     * 
     * @param serialNumber the serial number of the bunny to search for.
     * @return the bunny whose serial number is <tt>serialNumber</tt>, as a NabazTag/Tag wrapping object reference.
     * @throws NoSuchBunnyException if there is no bunny whose serial number is <tt>serialNumber</tt> in the burrow.
     */
    public Bunny getBunny(String serialNumber) throws NoSuchBunnyException
    {
	Bunny nabazTag = this.bunnies.get(serialNumber);
	if (nabazTag == null) throw new NoSuchBunnyException();
	return nabazTag;
    }

    /**
     * Checking if a bunny is currently in the burrow, given its serial number.
     * 
     * @param serialNumber the serial number of the bunny to search for.
     * @return <tt>true</tt> if there is a bunny whose serial number is <tt>serialNumber</tt> in the burrow, <tt>false</tt> if not.
     * 
     */
    public boolean isBunnyInBurrow(String serialNumber)
    {
	return this.bunnies.containsKey(serialNumber);
    }

    /**
     * Checking if a bunny is currently in the burrow.
     * 
     * @param bunny the bunny to search for an equivalent (i.e. same serial number) in the burrow.
     * @return <tt>true</tt> if there is a bunny equivalent to <tt>bunny</tt> in the burrow, <tt>false</tt> if not.
     */
    public boolean isBunnyInBurrow(Bunny bunny)
    {
	return this.isBunnyInBurrow(bunny.getSerialNumber());
    }
}
