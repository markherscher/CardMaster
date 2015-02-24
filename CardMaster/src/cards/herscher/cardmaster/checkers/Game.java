package cards.herscher.cardmaster.checkers;

public class Game
{
    public interface Listener
    {
        public void onPieceMoved();
        public void onGameWon();
        public void onGameConceded();
    }
    
    private final Player me;
    private final Player opponent;
    
    public void movePiece()
    {
        
    }
    
    public void concedeGame()
    {
        
    }
}
