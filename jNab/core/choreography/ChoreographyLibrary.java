package jNab.core.choreography;

import jNab.core.exceptions.NoSuchChoreographyException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Container for choreographies.
 * 
 * @author Juha-Pekka Rajaniemi
 * @author Ville Antila
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class ChoreographyLibrary
{
    /**
     * Set of stored choreographies.
     */
    private Set<Choreography> choreographies;

    /**
     * Creating a new choreography library instance, with an empty initial set of stored choreographies.
     */
    public ChoreographyLibrary()
    {
	this.choreographies = Collections.synchronizedSet(new HashSet<Choreography>());
    }

    /**
     * Registering a choreography.
     * 
     * @param choreography the choreography to register.
     */
    public void registerChoreography(Choreography choreography)
    {
	this.choreographies.add(choreography);
	choreography.setChoreographyLibrary(this);
    }

    /**
     * Unregistering a choreography.
     * 
     * @param choreography the choreography to unregister.
     * @throws NoSuchChoreographyException if no choreography whose name is <tt>name</tt> is registered.
     */
    public void unregisterChoreography(Choreography choreography) throws NoSuchChoreographyException
    {
	if (!this.choreographies.remove(choreography)) throw new NoSuchChoreographyException();
	choreography.setChoreographyLibrary(null);
    }

    /**
     * Getting a choreography, given its name.
     * 
     * @param name the name of the choreography to search for.
     * @return the choreography whose name is <tt>name</tt>.
     * @throws NoSuchChoreographyException if no choreography whose name is <tt>name</tt> is registered.
     * 
     */
    public Choreography getChoreography(String name) throws NoSuchChoreographyException
    {
	for (Choreography c : this.choreographies)
	    if (c.getName().equals(name)) return c;
	throw new NoSuchChoreographyException();
    }

    /**
     * Getting the set of registered choreographies.
     * 
     * @return the set of registered choreographies.
     */
    public Set<Choreography> getChoreographies()
    {
	return this.choreographies;
    }
}