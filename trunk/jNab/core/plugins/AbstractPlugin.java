package jNab.core.plugins;

import jNab.core.bunny.Bunny;
import jNab.core.misc.Couple;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class that all bunny plugins must extend.
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public abstract class AbstractPlugin
{
    /**
     * Bunny to which this plugin belongs.
     */
    protected Bunny bunny;

    /**
     * Name of the plugin.
     */
    protected String name;

    /**
     * Map containing the parameters of this plugin. The key is a parameter name, and the value is a couple whose first element is a boolean
     * indicating if the parameter is set and whose second element is the parameter value (if set).
     */
    protected Map<String, Couple<Boolean, String>> parameters;

    /**
     * Creating a new plugin instance.
     * 
     * @param pluginName the name of the plugin.
     * @param parameterNames the names of parameters supported by this plugin.
     */
    public AbstractPlugin(String pluginName, String[] parameterNames)
    {
	// Initially, the plugin is not attached to any bunny
	this.bunny = null;

	this.name = pluginName;
	this.parameters = new HashMap<String, Couple<Boolean, String>>();
	for (String parameterName : parameterNames)
	{
	    this.parameters.put(parameterName, new Couple<Boolean, String>(false, null));
	}
    }

    /**
     * Setting the bunny to which this plugin belongs.
     * 
     * @param bunny the bunny to which this plugin belongs.
     */
    public void setBunny(Bunny bunny)
    {
	this.bunny = bunny;
    }

    /**
     * Getting the bunny to which this plugin belongs.
     * 
     * @return the bunny to which this plugin belongs.
     */
    public Bunny getBunny()
    {
	return this.bunny;
    }

    /**
     * Checking that a parameter is available for this plugin.
     * 
     * @param paramName the name of the parameter.
     * @return <tt>true</tt> if the parameter whose name is <tt>paramName</tt> is supported by this plugin, <tt>false</tt> else.
     */
    public boolean isParameterValid(String paramName)
    {
	return this.parameters.containsKey(paramName);
    }

    /**
     * Setting the list of parameters available for this plugin.
     * 
     * @param parameters the list of parameters available for this plugin (parameters values can be set).
     */
    public void setParameters(Map<String, Couple<Boolean, String>> parameters)
    {
	this.parameters = parameters;
    }

    /**
     * Setting a parameter value for this plugin.
     * 
     * @param paramName the name of the parameter to set
     * @param paramValue the value to set for this parameter.
     * @return <tt>true</tt> if the parameter value has been set, <tt>false</tt> if no the parameter whose name is <tt>paramName</tt> is
     *         supported by this plugin.
     */
    public boolean setParameter(String paramName, String paramValue)
    {
	if (this.isParameterValid(paramName))
	{
	    Couple<Boolean, String> couple = this.parameters.get(paramName);
	    couple.setFirstElement(true);
	    couple.setSecondElement(paramValue);
	    return true;
	}
	return false;
    }

    /**
     * Getting the set of names of parameters supported by this plugin.
     * 
     * @return the set of names of parameters supported by this plugin.
     */
    public Set<String> getParameterNames()
    {
	return this.parameters.keySet();
    }

    /**
     * Getting the list of parameters available for this plugin.
     * 
     * @return the list of parameters available for this plugin.
     */
    public Map<String, Couple<Boolean, String>> getParameters()
    {
	return this.parameters;
    }

    /**
     * Testing if a given parameter is set.
     * 
     * @param paramName the name of the parameter.
     * @return <tt>true</tt> is the parameter whose name is <tt>paramName</tt> is set, <tt>false</tt> else.
     */
    public boolean isParameterSet(String paramName)
    {
	if (this.isParameterValid(paramName))
	{
	    return this.parameters.get(paramName).getFirstElement();
	}
	return false;
    }

    /**
     * Getting the value set for a parameter of this plugin.
     * 
     * @param paramName the name of the parameter whose value is requested.
     * @return the value set for the parameter called <tt>paramName</tt>, or <tt>null</tt> if no value is setor if parameter is invalid.
     */
    public String getParameterValue(String paramName)
    {
	if (this.isParameterSet(paramName)) return this.parameters.get(paramName).getSecondElement();
	return null;
    }

    /**
     * Getting the name of the plugin.
     * 
     * @return the name of the plugin.
     */
    public String getName()
    {
	return this.name;
    }

    /**
     * N.B. two plugins are equal if their names are the same.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o)
    {
	AbstractPlugin plugin = null;
	try
	{
	    plugin = (AbstractPlugin) o;
	}
	catch (ClassCastException e)
	{
	    return false;
	}
	return this.getName().equals(plugin.getName());
    }
}
