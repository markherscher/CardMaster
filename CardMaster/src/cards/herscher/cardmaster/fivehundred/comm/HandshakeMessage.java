package cards.herscher.cardmaster.fivehundred.comm;

public class HandshakeMessage extends Message
{
    public final static int ID = 0x01;
    
    private final int version;
    
    public HandshakeMessage()
    {
        super(ID);
        
        version = 1;
    }
    
    public int getVersion()
    {
        return version;
    }
}
