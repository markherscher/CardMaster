package cards.herscher.cardmaster.fivehundred;

import cards.herscher.cardmaster.CardCollection;

/**
 * Created by MarkHerscher on 10/28/2014.
 */
public class Player
{
	private final String name;
	private final CardCollection hand;
	private int tricksTaken;

	public Player(String name)
	{
		this(name, new CardCollection());
	}

	public Player(String name, CardCollection hand)
	{
		if (name == null)
		{
			throw new IllegalArgumentException("name cannot be null");
		}

		if (hand == null)
		{
			throw new IllegalArgumentException("hand cannot be null");
		}

		this.name = name;
		this.hand = hand;
	}

	public String getName()
	{
		return name;
	}

	public CardCollection getHand()
	{
		return hand;
	}

	public int getTricksTaken()
	{
		return tricksTaken;
	}

	protected void setTricksTaken(int value)
	{
		if (value < 0)
		{
			throw new IllegalArgumentException("value must be >= 0");
		}

		this.tricksTaken = tricksTaken;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
