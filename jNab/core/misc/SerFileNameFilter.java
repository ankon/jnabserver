package jNab.core.misc;

import java.io.File;
import java.io.FilenameFilter;

/**
 * File name filter for serialized bunnies (.ser) files.
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class SerFileNameFilter implements FilenameFilter
{
    /**
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    public boolean accept(File dir, String name)
    {
	return name.endsWith(".ser");
    }
}
