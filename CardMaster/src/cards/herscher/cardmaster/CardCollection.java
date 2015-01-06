package cards.herscher.cardmaster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by MarkHerscher on 10/28/2014.
 */
public class CardCollection implements Iterable<Card>
{
	private final List<Card> cardList;
	private final List<Listener> listenerList;

	public CardCollection()
	{
		cardList = new ArrayList<Card>();
		listenerList = new CopyOnWriteArrayList<Listener>();
	}

	public Card peekTop()
	{
		return count() > 0 ? cardList.get(0) : null;
	}

	public Card takeTop()
	{
		Card card = null;

		if (count() > 0)
		{
			card = cardList.get(0);
			cardList.remove(0);
			fireChangedListener();
		}

		return card;
	}

	public Card get(int index)
	{
		if (index < 0 || index >= count())
		{
			throw new IllegalArgumentException(
					String.format("invalid index %d (size is %d)", index, count()));
		}

		return cardList.get(index);
	}

	public void setAll(Collection<Card> cards)
	{
		if (cards == null)
		{
			throw new IllegalArgumentException("collection cannot be null");
		}

		cardList.clear();
		cardList.addAll(cards);
		fireChangedListener();
	}

	public void setAll(Card[] cards)
	{
		if (cards == null)
		{
			throw new IllegalArgumentException("array cannot be null");
		}

		cardList.clear();

		for (Card c : cards)
		{
			cardList.add(c);
		}

		fireChangedListener();
	}

	public void add(Card card)
	{
		if (card == null)
		{
			throw new IllegalArgumentException("card cannot be null");
		}

		if (cardList.contains(card))
		{
			throw new IllegalArgumentException(
					String.format("card %s already added", card.toString()));
		}

		cardList.add(card);
		fireChangedListener();
	}

	public void remove(Card card)
	{
		if (card == null)
		{
			throw new IllegalArgumentException("card cannot be null");
		}

		if (cardList.remove(card))
		{
			fireChangedListener();
		}
	}

	public void sort()
	{
		Collections.sort(cardList);
		fireChangedListener();
	}

	public void shuffle()
	{
		Collections.shuffle(cardList);
		fireChangedListener();
	}

	public int count()
	{
		return cardList.size();
	}

	public void addListener(Listener l)
	{
		if (l == null)
		{
			throw new IllegalArgumentException("listener cannot be null");
		}

		if (!listenerList.contains(l))
		{
			listenerList.add(l);
		}
	}

	public void removeListener(Listener l)
	{
		if (l == null)
		{
			throw new IllegalArgumentException("listener cannot be null");
		}

		listenerList.remove(l);
	}

	@Override
	public Iterator<Card> iterator()
	{
		return cardList.iterator();
	}

	@Override
	public String toString()
	{
		return String.format("count %d", count());
	}

	private void fireChangedListener()
	{
		for (Listener l : listenerList)
		{
			l.onModified(this);
		}
	}

	public interface Listener
	{
		public void onModified(CardCollection sender);
	}
}
