import jNab.core.choreography.Choreography;
import jNab.ext.persistency.Serializer;

import java.io.File;
import java.io.IOException;

/**
 * Exemple of application generating a choreography.
 * 
 * This choreography consists in putting down the ears and making the belly leds blink in red at 1Hz for 20s.
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class RedBlinkingFor20sChoreographyMaker
{
    /**
     * Application's main
     * @param args <i>(unused)</i>.
     */
    public static void main(String[] args)
    {
	System.out.println("Generating choreography");
	
	// Creating a new choreography
	Choreography choreography = new Choreography("RedBlinking");

	// Setting tempo frequency to 1s (100/100 Hz)
	choreography.addTempoCommand(0, 100);

	// Putting down both ears
	choreography.addAbsoluteEarMoveCommand(0, Choreography.EAR_LEFT, 10, Choreography.DIRECTION_FORWARD);
	choreography.addAbsoluteEarMoveCommand(0, Choreography.EAR_RIGHT, 10, Choreography.DIRECTION_FORWARD);
	
	// Making the belly leds blink in read at 1Hz for 20s
	choreography.addLedColorCommand(0, Choreography.LED_LEFT, 255, 0, 0);
	for (int i = 0; i < 9; i++)
	{
	    choreography.addLedColorCommand(0, Choreography.LED_CENTER, 255, 0, 0);
	    choreography.addLedColorCommand(0, Choreography.LED_RIGHT, 255, 0, 0);
	    choreography.addLedColorCommand(1, Choreography.LED_LEFT, 0, 0, 0);
	    choreography.addLedColorCommand(0, Choreography.LED_CENTER, 0, 0, 0);
	    choreography.addLedColorCommand(0, Choreography.LED_RIGHT, 0, 0, 0);
	    if (i < 9) choreography.addLedColorCommand(1, Choreography.LED_LEFT, 255, 0, 0);
	}

	// Saving the choreography to a file (here destination file is ./files/choreographies/RedBlinking.chor)
	Serializer serializer = new Serializer(new File("./files"));
	try
	{
	    serializer.saveChoreography(choreography);
	}
	catch (IOException e)
	{
	    System.err.println("unable to save choreography ("+e.getMessage()+")");
	    return;
	}
	System.out.println("Done");
    }
}
