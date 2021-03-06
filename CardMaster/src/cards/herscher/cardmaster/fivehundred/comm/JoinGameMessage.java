package cards.herscher.cardmaster.fivehundred.comm;

import cards.herscher.comm.message.Message;

public class JoinGameMessage extends Message
{
    private final String name;
    
    /**
     * Required for serialization. Do not use directly.
     */
    public JoinGameMessage()
    {
        this("");
    }
    
    public JoinGameMessage(String name)
    {
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
