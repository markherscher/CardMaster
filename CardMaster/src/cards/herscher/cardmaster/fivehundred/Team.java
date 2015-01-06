package cards.herscher.cardmaster.fivehundred;

/**
 * Created by MarkHerscher on 11/3/2014.
 */
public class Team
{
	private final Player player1;
	private final Player player2;
	private int score;

	public Team(Player player1, Player player2)
	{
		if (player1 == null || player2 == null)
		{
			throw new IllegalArgumentException("players cannot be null");
		}

		this.player1 = player1;
		this.player2 = player2;
		score = 0;
	}

	public Player getPlayer1()
	{
		return player1;
	}

	public Player getPlayer2()
	{
		return player2;
	}

	public int getScore()
	{
		return score;
	}

	public int getTricksTaken()
	{
		return player1.getTricksTaken() + player2.getTricksTaken();
	}

	protected void addToScore(int value)
	{
		score += value;
	}
}
