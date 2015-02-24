package cards.herscher.commtester;

import java.util.Locale;

import cards.herscher.comm.message.Message;

public class MathQuestionMessage extends Message 
{
    private final int number1;
    private final int number2;
    
    public MathQuestionMessage()
    {
        this(0, 0);
    }
    
    public MathQuestionMessage(int number1, int number2)
    {
        this.number1 = number1;
        this.number2 = number2;
    }
    
    public int getNumber1()
    {
        return number1;
    }
    
    public int getNumber2()
    {
        return number2;
    }
    
    @Override
    public String toString()
    {
        return String.format(Locale.US, "MathQuestionMessage %d (%d, %d)", getId(), number1, number2);
    }
}
