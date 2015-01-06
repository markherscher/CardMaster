package cards.herscher.cardmaster;

/**
 * Created by MarkHerscher on 10/28/2014.
 */
public abstract class Card implements Comparable
{
	private final int id;

	public Card(int id)
	{
		this.id = id;
	}

	@Override
	public String toString()
	{
		return id + "";
	}

	public int getId()
	{
		return id;
	}
}
