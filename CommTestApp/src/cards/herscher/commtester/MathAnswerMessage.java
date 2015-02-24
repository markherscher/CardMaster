package cards.herscher.commtester;

import java.util.Locale;

import cards.herscher.comm.message.Message;

public class MathAnswerMessage extends Message
{
    private final int answer;
    
    public MathAnswerMessage()
    {
        this(0);
    }
    
    public MathAnswerMessage(int answer)
    {
        this.answer = answer;
    }
    
    public int getAnswer()
    {
        return answer;
    }
    
    @Override
    public String toString()
    {
        return String.format(Locale.US, "MathAnswerMessage (%d)", answer);
    }
}
