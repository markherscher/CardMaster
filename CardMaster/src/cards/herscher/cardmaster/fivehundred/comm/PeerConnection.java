package cards.herscher.cardmaster.fivehundred.comm;

import java.net.Socket;

public class PeerConnection
{
    private final Socket socket;
    
    public PeerConnection(Socket socket)
    {
        if (socket == null)
        {
            throw new IllegalArgumentException();
        }
        
        this.socket = socket;
    }
}
