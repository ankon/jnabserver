package jNab.core.events;

/**
 * Interface for plugins handling ping event.
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public interface PingEventListener
{
    /**
     * Callback method used to process simple ping event.
     */
    public void onPing();
}
