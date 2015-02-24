package cards.herscher.cardmaster.checkers.comm;

import cards.herscher.comm.message.Message;

public class JoinGameMessage extends Message
{
    private final String playerName;
    
    /**
     * Required for serialization. Do not use directly.
     */
    public JoinGameMessage()
    {
        this("");
    }
    
    public JoinGameMessage(String playerName)
    {
        if (playerName == null)
        {
            throw new IllegalArgumentException();
        }
        
        this.playerName = playerName;
    }

    public String getPlayerName()
    {
        return playerName;
    }
}
