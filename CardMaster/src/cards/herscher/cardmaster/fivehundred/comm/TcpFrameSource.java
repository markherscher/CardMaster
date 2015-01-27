package cards.herscher.cardmaster.fivehundred.comm;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import cards.herscher.cardmaster.Logger;
import cards.herscher.cardmaster.comm.Frame;

/**
 * This class is not thread-safe.
 * @author MarkHerscher
 *
 */
public class TcpFrameSource implements FrameSource
{
    private final static String TAG = "TcpFrameSource";
    
    private final Socket socket;
    private final Frame frame;
    private InputStream inputStream;
    private byte[] inputBuf;
    
    public TcpFrameSource(Socket socket)
    {
        if (socket == null)
        {
            throw new IllegalArgumentException();
        }
        
        this.socket = socket;
        inputBuf = new byte[Frame.MAX_FRAME_LENGTH * 2];
        frame = new Frame(new FrameListener());
    }
    
    @Override
    public void init() throws IOException
    {
        inputStream = socket.getInputStream();
    }

    @Override
    public List<byte[]> readFrames() throws IOException
    {
        ArrayList<byte[]> frameList = new ArrayList<byte[]>();
        
        if (inputStream == null)
        {
            throw new IllegalStateException();
        }
        
        int readCount = inputStream.read(inputBuf);
        boolean isFrameAvailable = frame.parse(inputBuf, readCount);
        
        if (isFrameAvailable)
        {
            byte[] frameBytes = frame.getNextReceivedFrame();
            
            if (frameBytes != null)
            {
                frameList.add(frameBytes);
            }
        }
        
        return frameList;
    }

    private class FrameListener implements Frame.Listener
    {
        @Override
        public void onInvalidFrame()
        {
            Logger.w(TAG, "Invalid frame received");
        }

        @Override
        public void onBytesDiscarded()
        {
            Logger.w(TAG, "Bytes discarded due to length");
        }
    }
}
