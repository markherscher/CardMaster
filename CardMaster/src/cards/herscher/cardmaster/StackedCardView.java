package cards.herscher.cardmaster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MarkHerscher on 10/6/2014.
 */
public class StackedCardView extends RelativeLayout
{
	private final static int CARD_HEIGHT = 400;
	private final static int CARD_WIDTH = 200;

	private final List<Integer> cardList;

	public StackedCardView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		cardList = new ArrayList<Integer>();
	}

	public void setCards(List<Integer> cardList)
	{
		if (cardList == null)
		{
			throw new IllegalArgumentException();
		}

		this.cardList.addAll(cardList);
		removeAllViews();
		addAllViews();
		invalidate();
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
	}

	private void addAllViews()
	{
		final int X_OFFSET = 400 / cardList.size();
		int x = 0;

		for (int i = 0; i < cardList.size(); i++)
		{
			TextView view = new TextView(getContext());
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(CARD_WIDTH,
					CARD_HEIGHT);
			params.setMargins(i * X_OFFSET, 0, 0, 0);
			view.setText(i + "");

			if (i % 2 == 0)
			{
				view.setBackgroundColor(Color.BLUE);
			}
			else
			{
				view.setBackgroundColor(Color.RED);
			}

			addView(view, params);
		}
	}
}
