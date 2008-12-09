package jNab.core.events;

/**
 * Interface for plugins handling stopping type events.
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public interface StopEventListener
{
    /**
     * Callback method used to process single-click while playing event.
     */
    public void onSingleClickWhilePlaying();

    /**
     * Callback method used to process end of message event.
     */
    public void onEndOfMessage();
}
