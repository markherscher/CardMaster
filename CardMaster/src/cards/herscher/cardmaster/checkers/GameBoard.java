package cards.herscher.cardmaster.checkers;

import java.util.ArrayList;
import java.util.List;

public class GameBoard
{
    public final static int X_SQUARES = 8;
    public final static int Y_SQUARES = 8;
    
    private final GamePiece[][] squares;
    
    public GameBoard()
    {
        squares = new GamePiece[X_SQUARES][Y_SQUARES];
    }
    
    public GamePiece getPieceOnSquare(Location location)
    {
        if (location == null)
        {
            throw new IllegalArgumentException();
        }
        
        return squares[location.getX()][location.getY()];
    }
    
    public Location[] getValidMoves(GamePiece piece, boolean isAfterJump)
    {
        if (piece == null)
        {
            throw new IllegalArgumentException();
        }
        
        List<Location> validMoves = new ArrayList<Location>();
        Location currentLocation = piece.getLocation();
        
        if (currentLocation.y < Y_SQUARES)
        {
            // Move ahead left
            if (currentLocation.x > 0)
            {
                
            }
        }
    }
    
    public boolean canMovePiece(GamePiece piece, Location location)
    {
        if (location == null)
        {
            throw new IllegalArgumentException();
        }
        
        // Make sure the location isn't occupied
        if (getPieceOnSquare(location) != null)
        {
            return false;
        }
        
        
        
        return true;
    }
    
    public boolean movePiece(GamePiece piece, int newX, int newY)
    {
        return true;
    }
    
    public GamePiece[] getJumpablePieces(GamePiece piece)
    {
        return null;
    }
    
    public void removePiece(GamePiece piece)
    {
        
    }
    
    public void removePiece(int x, int y)
    {
        
    }
    
    public static class Location
    {
        private final int x;
        private final int y;
        
        public Location(int x, int y)
        {
            if (x < 0 || x >= X_SQUARES || y < 0 || y >= Y_SQUARES)
            {
                throw new IllegalArgumentException();
            }
            
            this.x = x;
            this.y = y;
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }
    }
}
