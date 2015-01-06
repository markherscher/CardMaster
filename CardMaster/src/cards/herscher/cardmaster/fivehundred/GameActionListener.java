package cards.herscher.cardmaster.fivehundred;

/**
 * Created by MarkHerscher on 10/28/2014.
 */
public interface GameActionListener
{
	public void requestBid(Game game, Player player);

	public void requestLead(Game game, Player player);

	public void requestPlay(Game game, Player player);

	public void requestDiscard(Game game, Player player);
}
