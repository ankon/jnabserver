package jNab.core.events;

/**
 * Interface for plugins handling ears move event.
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public interface EarsEventListener
{
    /**
     * Call-back method used to process ears movement event.
     * 
     * @param rightEar new position of the right ear.
     * @param leftEar new position of the left ear.
     */
    public void onEarsMove(int rightEar, int leftEar);
}
