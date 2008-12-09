import jNab.core.server.MicroServer;
import jNab.ext.configuration.ServerConfigurationServer;
import jNab.ext.persistency.Serializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Application running an instance of jNab's micro server.
 * 
 * @author Juha-Pekka Rajaniemi
 * @author Ville Antila
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class jNab
{
    /**
     * Default value for the port to which the server is bound.
     */
    public final static String DEFAULT_SERVER_PORT = "8080";

    /**
     * Default value for the port to which the configuration server is bound.
     */
    public final static String DEFAULT_CONF_PORT = "6969";

    /**
     * Default path for serialized files.
     */
    private final static String DEFAULT_SERIALIZATION_PATH = "files/";

    /**
     * Default path for resource files.
     */
    private final static String DEFAULT_RESOURCE_PATH = "files/";

    /**
     * Default path for plugins.
     */
    private final static String DEFAULT_PLUGIN_PATH = "files/plugins";

    /**
     * Running an instance of MicroServer. Server properties are set by loading <tt>jNab.conf</tt> configuration file. Loaded properties can
     * be overridden by command line arguments. Command-lines arguments can be either :
     * <ul>
     * <li>-server.ip=<i>value</i></li> for setting/overriding server's binding ip
     * <li>-server.port=<i>value</i></li> for setting/overriding server's binding port
     * <li>-conf.ip=<i>value</i></li> for setting/overriding configuration server's binding ip
     * <li>-conf.port=<i>value</i></li> for setting/overriding configuration server's binding port
     * <li>-serializedfiles.root=<i>value</i></li> for setting/overriding serialization root path
     * <li>-resources.root=<i>value</i></li> for setting/overriding resources root path
     * <li>-plugins.root=<i>value</i></li> for setting/overriding plugins root path
     * </ul>
     * 
     * @param args command-line arguments.
     */
    public static void main(String[] args)
    {

	// Setting system properties to default values
	System.setProperty("jNab.server.ip", "");
	System.setProperty("jNab.server.port", DEFAULT_SERVER_PORT);
	System.setProperty("jNab.conf.ip", "");
	System.setProperty("jNab.conf.port", DEFAULT_CONF_PORT);
	System.setProperty("jNab.serializedfiles.root", DEFAULT_SERIALIZATION_PATH);
	System.setProperty("jNab.resources.root", DEFAULT_RESOURCE_PATH);
	System.setProperty("jNab.plugins.root", DEFAULT_PLUGIN_PATH);

	// Reading property file
	Properties properties = new Properties();
	try
	{
	    properties.load(new FileInputStream("jNab.conf"));
	    System.out.println("<jNab> jNab configuration file read");
	}
	catch (FileNotFoundException e)
	{
	    System.out.println("<jNab> Configuration file not found, setting server properties to default values");
	}
	catch (IOException e)
	{
	    System.out.println("<jNab> Configuration file is corrupted, ignoring it");
	}

	// Re-setting system properties with properties loaded from configuration file

	String property = null;

	property = properties.getProperty("jNab.server.ip");
	if (property != null) System.setProperty("jNab.server.ip", property);

	property = properties.getProperty("jNab.server.port");
	if (property != null) System.setProperty("jNab.server.port", property);

	property = properties.getProperty("jNab.conf.ip");
	if (property != null) System.setProperty("jNab.conf.ip", property);

	property = properties.getProperty("jNab.conf.port");
	if (property != null) System.setProperty("jNab.conf.port", property);

	property = properties.getProperty("jNab.serializedfiles.root");
	if (property != null) System.setProperty("jNab.serializedfiles.root", property);

	property = properties.getProperty("jNab.resources.root");
	if (property != null) System.setProperty("jNab.resources.root", property);

	property = properties.getProperty("jNab.plugins.root");
	if (property != null) System.setProperty("jNab.plugins.root", property);

	// Re-setting system properties with properties overridden by command-line arguments

	for (String parameter : args)
	{
	    if (parameter.startsWith("-server.ip=")) System.setProperty("jNab.server.ip", parameter.substring(parameter.indexOf('=') + 1));
	    if (parameter.startsWith("-server.port="))
		System.setProperty("jNab.server.port", parameter.substring(parameter.indexOf('=') + 1));
	    if (parameter.startsWith("-conf.ip=")) System.setProperty("jNab.conf.ip", parameter.substring(parameter.indexOf('=') + 1));
	    if (parameter.startsWith("-conf.port=")) System.setProperty("jNab.conf.port", parameter.substring(parameter.indexOf('=') + 1));
	    if (parameter.startsWith("-serializedfiles.root="))
		System.setProperty("jNab.serializedfiles.root", parameter.substring(parameter.indexOf('=') + 1));
	    if (parameter.startsWith("-resources.root="))
		System.setProperty("jNab.resources.root", parameter.substring(parameter.indexOf('=') + 1));
	    if (parameter.startsWith("-plugins.root="))
		System.setProperty("jNab.plugins.root", parameter.substring(parameter.indexOf('=') + 1));
	}

	// Binding and starting server
	String serverAddress = System.getProperty("jNab.server.ip");
	int serverPort = 0;
	try
	{
	    serverPort = Integer.parseInt(System.getProperty("jNab.server.port"));
	}
	catch (NumberFormatException e1)
	{
	    System.err.println("<jNab> Server port has an invalid value, exiting...");
	    System.exit(-1);
	}

	File resourcesPath = new File(System.getProperty("jNab.resources.root"));
	File pluginsPath = new File(System.getProperty("jNab.plugins.root"));

	// Creating (but not starting yet) a micro server instance
	MicroServer microServer = new MicroServer(serverAddress, serverPort, resourcesPath, pluginsPath);

	// Creating a serialization service instance
	Serializer serializer = new Serializer(new File(System.getProperty("jNab.serializedfiles.root")));
	System.out.println("<jNab> Serialization service started");

	// Unserializing choreographies
	System.out.println("<jNab> Reading serialized choreographies");
	serializer.loadChoreographies(microServer.getChoregraphyLibrary());

	// Unserializing bunnies
	System.out.println("<jNab> Reading serialized bunnies");
	serializer.loadBunnies(microServer.getBurrow(), microServer.getPluginFactory());

	// Starting micro server
	microServer.start();

	// Enabling all logging features, using stderr and stdout as logging stream
	microServer.setDebugLoggingStream(System.out);
	microServer.setErrorLoggingStream(System.err);
	microServer.setInfoLoggingStream(System.out);

	System.out.println("<jNab> Server properties settings:");
	System.out.println("<jNab> jNab.server.ip = " + System.getProperty("jNab.server.ip"));
	System.out.println("<jNab> jNab.server.port = " + System.getProperty("jNab.server.port"));
	System.out.println("<jNab> jNab.conf.ip = " + System.getProperty("jNab.conf.ip"));
	System.out.println("<jNab> jNab.conf.port = " + System.getProperty("jNab.conf.port"));
	System.out.println("<jNab> jNab.serializedfiles.root = " + System.getProperty("jNab.serializedfiles.root"));
	System.out.println("<jNab> jNab.resources.root = " + System.getProperty("jNab.resources.root"));
	System.out.println("<jNab> jNab.plugins.root = " + System.getProperty("jNab.plugins.root"));

	// Binding and starting a server configuration server
	String confAddress = System.getProperty("jNab.conf.ip");
	int confPort = 0;
	try
	{
	    confPort = Integer.parseInt(System.getProperty("jNab.conf.port"));
	}
	catch (NumberFormatException e1)
	{
	    System.err.println("<jNab> Configuration port has an invalid value, exiting...");
	    System.exit(-1);
	}
	new ServerConfigurationServer(confAddress, confPort, microServer, serializer).start();
    }
}
