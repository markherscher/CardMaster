package cards.herscher.comm.message;

import java.util.Locale;

public class ResponseMessage extends Message
{
    private final int originalMessageId;
    private final Message actualMessage;

    /**
     * Required for serialization. Do not use directly.
     */
    public ResponseMessage()
    {
        this(0, null);
    }

    public ResponseMessage(int originalMessageId, Message actualMessage)
    {
        this.originalMessageId = originalMessageId;
        this.actualMessage = actualMessage;
    }

    public Message getActualMessage()
    {
        return actualMessage;
    }

    public int getOriginalMessageId()
    {
        return originalMessageId;
    }

    @Override
    public String toString()
    {
        return String.format(Locale.US, "ResponseMessage for %d (%s)", originalMessageId,
                actualMessage.toString());
    }
}
