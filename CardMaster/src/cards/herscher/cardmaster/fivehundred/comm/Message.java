package cards.herscher.cardmaster.fivehundred.comm;

import java.io.Serializable;

public class Message implements Serializable
{
    private static final long serialVersionUID = -3413099676597200828L;
    private final int id;

    public Message(int id)
    {
        this.id = id;
    }
    
    public final int getId()
    {
        return id;
    }
}
