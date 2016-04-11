# Introduction #

jNabServer is a Java based (and therefore OS independent) software, which enables developers to build applications using Nabaztag/tag bunnies without the Violet platform.

jNabServer is reasonably light weight implementation so that in addition to regular computers, it can be run even on some integrated platforms.

# Authors and Contributors #

Original jNabServer was written by Juha-Pekka Rajaniemi and Ville Antila under supervision of Markku Turunen. Jaakko Hakulinen has provided minor updates, support and maintanence. (Speech-based and Pervasive Interaction Group, Tampere Unit for Human Computer Interaction, Department of Computer Sciences, University of Tampere, Finland).

Relevant people in University of Tampere can be contacted with address: nabaztag@cs.uta.fi

Version 2 was written by Sylvain Gizard ( Engineering student, Telecommunications department, INSA Lyon, France) and Sébastien Jean (Associate Professor, LCIS lab., IUT Valence, France).

# Download #

Full download distributions contains source code, compiled classes, jar file, javadoc files, some compiled plugins and their manifestations, bootcode and configuration files.

Source files can be downloaded via svn.

# Usage #

Version 2 of jNabServer does not currently have documentation. Version 1 and all related documentation can be acccessed in: http://www.sis.uta.fi/~spi/jnabserver/

## Quickstart ##

Until proper documentation becomes available, here is a quick start:

Main class for jNabServer is jNab. It reads configuration from file jNab.conf.

Before running jNabServer, you must modify the configuration file by updating
the address where the server is to be bound. This must be the IP address assigned to the wlan network your Nabaztag uses to connect to your server. In windows, you can use ipconfig command to find the address.

After starting the server (e.g., java -cp lib\jNab.jar jNab), you can configure the server by connecting the port specified in configuration file. Telnet to the IP and port defined and you can register different plugins to different Nabaztags etc. Until documentation becomes available, see class jNab.ext.configuration.ServerConfigurationServer for available commands. Names of plugins included in the full distribution can be found from the corresponding manifest files.

If you want to integrate Nabaztag as part of a larger software, see file test.java in full distribution for an example.

### Writing plugins ###

To write your own funtionality, you need to write your own plugins. A plugin must extend AbstractPlugin and implement relevant interfaces from jNab.core.events.

To use a plugin with jNabServer, it must be compiled and packed into a jar file together with a manifest file. See included plugins and their manifests in the distribution.