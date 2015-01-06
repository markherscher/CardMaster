package cards.herscher.cardmaster.fivehundred;

import cards.herscher.cardmaster.Card;

/**
 * Created by MarkHerscher on 10/28/2014.
 */
public class Game
{
	public enum State
	{
		PRE_BIDING,
		BIDDING,
		LEADING,
		PLAYING,
		COMPLETE
	}

	private final Team team1;
	private final Team team2;
	// TODO: trump suit
	private State currentState;
	private StateImpl stateImpl;

	public Game()
	{
		team1 = null;
		team2 = null;
	}

	public boolean isCardLegalToPlay(Card card)
	{
		return false;
	}

	public Team getTeamForPlayer(Player player)
	{
		if (player == null)
		{
			throw new IllegalArgumentException("player cannot be null");
		}

		if (player == team1.getPlayer1() || player == team1.getPlayer2())
		{
			return team1;
		}
		else if (player == team2.getPlayer1() || player == team2.getPlayer2())
		{
			return team2;
		}
		else
		{
			throw new IllegalArgumentException("player is not a member of either team");
		}
	}

	private abstract class StateImpl
	{
		protected Player activePlayer;

		public abstract void activate();
	}
}
