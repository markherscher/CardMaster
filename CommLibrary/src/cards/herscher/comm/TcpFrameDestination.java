package cards.herscher.comm;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Locale;

/**
 * This class is not thread-safe.
 * 
 * @author MarkHerscher
 * 
 */
public class TcpFrameDestination implements FrameDestination
{
    private final Socket socket;
    private final Frame frame;
    private OutputStream outputStream;
    public TcpFrameDestination(Socket socket)
    {
        if (socket == null)
        {
            throw new IllegalArgumentException();
        }

        this.socket = socket;
        frame = new Frame();
    }

    @Override
    public void init() throws IOException
    {
        outputStream = socket.getOutputStream();
    }

    @Override
    public void sendFrame(byte[] rawBytes) throws IOException
    {
        if (rawBytes == null)
        {
            throw new IllegalArgumentException();
        }

        byte[] frameBytes = frame.create(rawBytes);
        outputStream.write(frameBytes);
    }

    @Override
    public String toString()
    {
        InetAddress inet = socket.getInetAddress();
        return String.format(Locale.US, "TcpFrameDestination (%s:%d)", inet == null ? "--" : inet.toString(),
                socket.getPort());
    }
}
