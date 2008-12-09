package jNab.core.events;

/**
 * Interface for plugins handling click type events.
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public interface ClickEventListener
{
    /**
     * Callback method used to process single-click event.
     */
    public void onSingleClick();

    /**
     * Callback method used to process double-click event.
     */
    public void onDoubleClick();
}
