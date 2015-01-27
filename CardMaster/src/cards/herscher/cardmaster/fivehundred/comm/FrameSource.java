package cards.herscher.cardmaster.fivehundred.comm;

import java.io.IOException;
import java.util.List;

public interface FrameSource
{
    /**
     * Initializes this instance. Calling more than once will result in undefined behavior.
     * 
     * @throws IOException
     */
    public void init() throws IOException;

    /**
     * Reads a frame, blocking until it is available. Multiple frames can be received in one read.
     * @return the frames received (will never be null)
     * @throws IOException
     */
    public List<byte[]> readFrames() throws IOException;
}
