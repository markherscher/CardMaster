package cards.herscher.cardmaster.checkers;

public class Player
{
    private final String name;
    private final int id;
    
    public Player(String name, int id)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name cannot be null");
        }
        
        this.name = name;
        this.id = id;
    }
    
    public String getName()
    {
        return name;
    }
    
    public int getId()
    {
        return id;
    }
    
    @Override
    public String toString()
    {
        return name;
    }
}
