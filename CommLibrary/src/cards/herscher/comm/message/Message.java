package cards.herscher.comm.message;

public abstract class Message
{
    private static int nextId = 0;
    
    private final int id;
    
    public Message()
    {
        id = nextId++;
    }
    
    public final int getId()
    {
        return id;
    }
}
