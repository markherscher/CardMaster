package cards.herscher.cardmaster.fivehundred;

import cards.herscher.cardmaster.Card;

/**
 * Created by MarkHerscher on 10/28/2014.
 */
public interface GameEventListener
{
	public void onBid(Player player); // TODO

	public void onBidWon();

	public void onCardsDiscarded(Player player, Card[] cards);

	public void onCardLead(Player player, Card card);

	public void onCardPlayed(Player player, Card card);

	public void onTrickWon(Player player); // TODO

	public void onRoundDone(); // TODO

	public void onScoreChanged();

	public void onGameOver();
}
