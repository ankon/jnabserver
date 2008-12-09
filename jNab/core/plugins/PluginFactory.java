package jNab.core.plugins;

import jNab.core.exceptions.NoSuchPluginException;
import jNab.core.exceptions.PluginCreationException;
import jNab.core.misc.JarFileNameFilter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * Factory for bunny plugins.<br/>
 * 
 * Creation of plugins instances using the jNab manifest entry.
 * 
 * Sample plugin Manifest: <br/><br/> <tt>
 * Manifest-Version: 1.0 <br/>
 * Name: jNab <br/> 
 * Plugin-Name: Talkie_Plugin <br/>
 * Plugin-MainClass: plugin.TalkiePlugin <br/>
 * Plugin-Type: RecordPlugin <br/>
 * Plugin-Parameters: receiver <br/>
 * Plugin-Actions: <br/>
 * </tt>
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class PluginFactory
{
    /**
     * Path for plugins jar files.
     */
    private File pluginsPath;

    /**
     * Map of available plugins. The key is a plugin name, the value is a map whose key is a plugin property name and the value is a plugin
     * property value.
     */
    private Map<String, Map<String, Object>> availablePlugins;

    /**
     * Creating a new plugin factory instance.
     * 
     * @param pluginsPath the path for plugins jar files.
     */
    public PluginFactory(File pluginsPath)
    {
	this.pluginsPath = pluginsPath;
	this.availablePlugins = new HashMap<String, Map<String, Object>>();
	this.refreshAvailablePlugins();
    }

    /**
     * Internal method used to build the inner map of available plugins.
     */
    private void refreshAvailablePlugins()
    {
	if (!(this.pluginsPath.isDirectory())) return;

	for (File pluginJarFile : this.pluginsPath.listFiles(new JarFileNameFilter()))
	{
	    try
	    {
		// Reading the jNab section of the manifest
		Attributes jNabSection = new JarFile(pluginJarFile).getManifest().getAttributes("jNab");

		// Initializing current plugin's information map
		Map<String, Object> currentPluginMap = new HashMap<String, Object>();

		// Adding Jar name
		currentPluginMap.put("jar", pluginJarFile.toURI().toURL());

		// Adding plugin Main class
		currentPluginMap.put("main", jNabSection.getValue("Plugin-MainClass"));

		// Adding plugin interfaces/type
		String[] pluginInterfacesTokens = jNabSection.getValue("Plugin-Type").split(" ");
		List<String> pluginInterfaces = new ArrayList<String>();
		for (String string : pluginInterfacesTokens)
		    if (!string.equals("")) pluginInterfaces.add(string);
		currentPluginMap.put("interfaces", pluginInterfaces);

		// Adding plugin parameters
		String[] pluginParametersTokens = jNabSection.getValue("Plugin-Parameters").split(" ");
		List<String> pluginParameters = new ArrayList<String>();
		for (String string : pluginParametersTokens)
		    if (!string.equals("")) pluginParameters.add(string);
		currentPluginMap.put("parameters", pluginParameters.toArray(new String[] {}));

		// Adding plugin actions
		String[] pluginActionsTokens = jNabSection.getValue("Plugin-Actions").split(" ");
		List<String> actions = new ArrayList<String>();
		for (String string : pluginActionsTokens)
		    if (!string.equals("")) actions.add(string);
		currentPluginMap.put("actions", actions);

		// Putting the plugin map in the available plugins map using the name
		// of the plugin as key
		this.availablePlugins.put(jNabSection.getValue("Plugin-Name"), currentPluginMap);

	    }
	    catch (IOException e)
	    {
		continue;
	    }
	}
    }

    /**
     * Creating a new plugin instance, given the plugin name.
     * 
     * @param pluginName the name of the plugin to create.
     * @return the plugin created using <tt>pluginName</tt> definition.
     * @throws PluginCreationException if plugin creation failed.
     */
    public AbstractPlugin createPlugin(String pluginName) throws PluginCreationException
    {
	AbstractPlugin plugin = null;

	Class<?> c = null;

	// Trying to load the main class, twice if a refreshing of the available
	// plugins is needed
	try
	{
	    c = this.loadPluginMainClass(pluginName);
	}
	catch (Exception e)
	{
	    refreshAvailablePlugins();
	    try
	    {
		c = loadPluginMainClass(pluginName);
	    }
	    catch (Exception e2)
	    {
		throw new PluginCreationException();
	    }
	}

	// Invoking the constructor without parameters
	try
	{
	    plugin = (AbstractPlugin) c.newInstance();
	}
	catch (Exception e)
	{
	    throw new PluginCreationException();
	}
	return plugin;
    }

    /**
     * Internal method loading the main class of a plugin.
     * 
     * @param pluginName the name of the plugin.
     * @return the main class of the plugin.
     * @throws NoSuchPluginException if no plugin called <tt>pluginName</tt> is available.
     * @throws ClassNotFoundException if the main class of the plugin could not be loaded.
     */
    private Class<?> loadPluginMainClass(String pluginName) throws NoSuchPluginException, ClassNotFoundException
    {
	// Getting plugin information
	Map<String, Object> pluginInfo = this.availablePlugins.get(pluginName);
	if (pluginInfo == null) throw new NoSuchPluginException();

	// Loading the main class with URLClassLoader
	URL jar = (URL) pluginInfo.get("jar");
	URLClassLoader ucl = new URLClassLoader(new URL[] { jar });
	return ucl.loadClass((String) pluginInfo.get("main"));
    }

    /**
     * Getting the map of available plugins (with parameters and actions). The return value is map whose key is a plugin name and whose
     * value is a map whose key is a string identifying an attribute (jar, main, ... see manifest for details) and the value its value.
     * 
     * @return the map of available plugins.
     */
    public Map<String, Map<String, Object>> getAvailablePlugins()
    {
	return this.availablePlugins;
    }

    /**
     * Getting the name of a plugin from the name of its main class.
     * 
     * @param mainClassName the name of the main class.
     * @return the name of the plugin whose main class is <tt>mainClassName</tt>.
     * @throws NoSuchPluginException if no plugin has <tt>mainClassName</tt> as main class.
     */
    public String getPluginName(String mainClassName) throws NoSuchPluginException
    {
	for (Entry<String, Map<String, Object>> availablePluginsEntries : this.availablePlugins.entrySet())
	    if (((Map<String, Object>) availablePluginsEntries.getValue()).get("main").equals(mainClassName))
		return availablePluginsEntries.getKey();
	throw new NoSuchPluginException();
    }
}
