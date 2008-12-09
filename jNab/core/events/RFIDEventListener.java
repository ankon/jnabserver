package jNab.core.events;

/**
 * Interface for plugins handling RFID detection event.
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public interface RFIDEventListener
{
    /**
     * Callback method used to process RFID detection event.
     * 
     * @param rfid ID of the tag read.
     */
    public void onRfid(String rfid);
}
