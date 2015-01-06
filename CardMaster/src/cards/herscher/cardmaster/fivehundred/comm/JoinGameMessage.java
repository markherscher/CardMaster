package cards.herscher.cardmaster.fivehundred.comm;

public class JoinGameMessage extends Message
{
    private static final long serialVersionUID = 8230636366232924254L;
    public final static int ID = 1;
  
    private final String name;
    
    public JoinGameMessage(String name)
    {
        super(ID);

        if (name == null || name.length() == 0)
        {
            throw new IllegalArgumentException();
        }
        
        this.name = name;
    }

    public String getPlayerName()
    {
        return name;
    }
}
