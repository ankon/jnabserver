package jNab.core.events;

/**
 * Interface for plugins handling voice recording events.
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public interface RecordEventListener
{
    /**
     * Callback method used to process a voice recording event (single long click).
     * 
     * @param data recorded voice data (WAV)
     */
    public void onSimpleRecord(byte[] data);

    /**
     * Callback method used to process a voice recording event (double long click).
     * 
     * @param data recorded voice data (WAV)
     */
    public void onDoubleRecord(byte[] data);
}
