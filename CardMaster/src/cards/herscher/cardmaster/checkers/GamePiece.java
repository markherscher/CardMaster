package cards.herscher.cardmaster.checkers;

public class GamePiece
{
    private Player owningPlayer;
    private GameBoard.Location location;
    private boolean isKing;
    
    public Player getOwner()
    {
        return owningPlayer;
    }
    
    public GameBoard.Location getLocation()
    {
        return location;
    }
    
    public boolean isKing()
    {
        return isKing;
    }
}
