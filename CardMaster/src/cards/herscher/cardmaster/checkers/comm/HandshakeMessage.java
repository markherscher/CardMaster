package cards.herscher.cardmaster.checkers.comm;

import cards.herscher.comm.message.Message;

public class HandshakeMessage extends Message
{
    private final int version;

    /**
     * Required for serialization. Do not use directly.
     */
    public HandshakeMessage()
    {
        this(1);
    }

    public HandshakeMessage(int version)
    {
        this.version = version;
    }

    public int getVersion()
    {
        return version;
    }
}
