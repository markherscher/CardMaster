package cards.herscher.comm;


import java.io.IOException;

public interface FrameDestination
{
    /**
     * Initializes this instance. Calling more than once will result in undefined behavior.
     * 
     * @throws IOException
     */
    public void init() throws IOException;

    /**
     * Writes the specified bytes out using the frame protocol.
     * 
     * @param rawBytes
     *            bytes to write (cannot be null)
     * @throws IOException
     */
    public void sendFrame(byte[] rawBytes) throws IOException;
}
