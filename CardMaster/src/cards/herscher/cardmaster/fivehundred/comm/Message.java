package cards.herscher.cardmaster.fivehundred.comm;

public abstract class Message
{
    public final static int INVALID_ID = 0;
    
    private final int id;

    public Message()
    {
        this(INVALID_ID); 
    }
    
    public Message(int id)
    {
        this.id = id;
    }
    
    public final int getId()
    {
        return id;
    }
}
