package cards.herscher.comm.message;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.os.Handler;
import android.util.SparseArray;

import cards.herscher.comm.FrameDestination;
import cards.herscher.comm.FrameSource;
import cards.herscher.comm.Logger;

/**
 * The public methods are not thread-safe.
 * 
 * @author MarkHerscher
 * 
 */
public class MessageConnection
{
    public interface Listener
    {
        public ResponseMessage onMessageReceived(Message message);

        public void onMessageResponseReceived(Message originalMessage,
                ResponseMessage responseMessage);

        public void onNoMessageResponse(Message message);

        public void onReceiveError(IOException e);

        public void onSendError(IOException e);
    }

    public final static int DEFAULT_RESPONSE_TIMEOUT = 5000;
    private final static String TAG = "MessageConnection";

    private final FrameSource frameSource;
    private final FrameDestination frameDest;
    private final MessageSerializer messageSerializer;
    private final Listener listener;
    private final SparseArray<OutstandingMessage> outstandingMessages;
    private final Handler handler;
    private final OutgoingRunnable outgoingRunnable;
    private final IncomingRunnable incomingRunnable;
    private boolean isOpen;
    private boolean isClosed;
    private int responseTimeoutMs;
    private String name;

    public MessageConnection(FrameSource frameSource, FrameDestination frameDest,
            MessageSerializer messageSerializer, Handler handler, Listener listener)
    {
        if (frameSource == null || frameDest == null || messageSerializer == null
                || handler == null)
        {
            throw new IllegalArgumentException();
        }

        this.frameSource = frameSource;
        this.frameDest = frameDest;
        this.messageSerializer = messageSerializer;
        this.listener = listener;
        this.handler = handler;
        outstandingMessages = new SparseArray<OutstandingMessage>();
        outgoingRunnable = new OutgoingRunnable();
        incomingRunnable = new IncomingRunnable();
        responseTimeoutMs = DEFAULT_RESPONSE_TIMEOUT;
        name = "";
    }

    public void open()
    {
        if (isClosed)
        {
            throw new IllegalStateException("already closed");
        }

        if (!isOpen)
        {
            isOpen = true;
            outgoingRunnable.start();
            incomingRunnable.start();
        }
    }

    public void close()
    {
        if (isOpen)
        {
            isOpen = false;
            isClosed = true;
            outgoingRunnable.stop();
        }
    }

    public void sendMessage(Message message, boolean responseExpected)
    {
        if (!isOpen)
        {
            return;
        }

        if (message == null)
        {
            throw new IllegalArgumentException();
        }

        OutstandingMessage outstandingMsg = null;

        if (responseExpected)
        {
            outstandingMsg = new OutstandingMessage(message);

            // Add to the outstanding messages collection before sending, to avoid racing if the
            // response is very fast
            synchronized (outstandingMessages)
            {
                outstandingMessages.put(message.getId(), outstandingMsg);
            }
        }

        outgoingRunnable.addMessage(message);

        // Start the timer after sending, to make it "fair"
        if (responseExpected)
        {
            handler.postDelayed(outstandingMsg.expireRunnable, responseTimeoutMs);
        }
    }

    public void setName(String name)
    {
        if (name == null)
        {
            name = "";
        }

        this.name = name;
    }

    public void setResponseTimeout(int timeout)
    {
        responseTimeoutMs = timeout;
    }

    public int getResponseTimeout()
    {
        return responseTimeoutMs;
    }

    public FrameSource getFrameSource()
    {
        return frameSource;
    }

    public FrameDestination getFrameDestination()
    {
        return frameDest;
    }

    public MessageSerializer getMessageSerializer()
    {
        return messageSerializer;
    }

    private void handleMessageReceived(Message receivedMessage)
    {
        Logger.d(TAG, "%s: Received message %s", name, receivedMessage.toString());

        if (receivedMessage instanceof ResponseMessage)
        {
            // Find a matching outstanding message
            int originalMessageId = ((ResponseMessage) receivedMessage).getOriginalMessageId();
            OutstandingMessage outMsg = null;

            synchronized (outstandingMessages)
            {
                outMsg = outstandingMessages.get(originalMessageId);
            }

            if (outMsg != null)
            {
                synchronized (outMsg)
                {
                    if (outMsg.isOutstanding)
                    {
                        outMsg.isOutstanding = false;
                        outstandingMessages.remove(originalMessageId);
                    }
                    else
                    {
                        return;
                    }
                }

                if (listener != null)
                {
                    listener.onMessageResponseReceived(outMsg.message,
                            (ResponseMessage) receivedMessage);
                }
            }
        }
        else if (listener != null)
        {
            ResponseMessage response = listener.onMessageReceived(receivedMessage);

            if (response != null)
            {
                sendMessage(response, false);
            }
        }
    }

    private class IncomingRunnable implements Runnable
    {
        public void start()
        {
            new Thread(this, "MessageConnection_Incoming").start();
        }

        @Override
        public void run()
        {
            while (isOpen)
            {
                List<byte[]> frameBytes = null;

                try
                {
                    frameBytes = frameSource.readFrames();
                }
                catch (IOException e)
                {
                    if (isOpen && listener != null)
                    {
                        listener.onReceiveError(e);
                    }
                }

                if (frameBytes != null)
                {
                    for (byte[] fb : frameBytes)
                    {
                        Message message = null;

                        // Deserialize the message
                        try
                        {
                            message = messageSerializer.deserialize(fb);
                        }
                        catch (IllegalArgumentException e)
                        {
                            if (isOpen)
                            {
                                Logger.e(TAG, "%s: Error deserializing message: %s", name,
                                        e.getMessage());
                            }
                        }

                        if (message != null)
                        {
                            handleMessageReceived(message);
                        }
                    }
                }
            }
        }
    }

    private class OutgoingRunnable implements Runnable
    {
        private final BlockingQueue<Message> messageQueue;
        private Thread myThread;

        public OutgoingRunnable()
        {
            messageQueue = new LinkedBlockingQueue<Message>();
        }

        public void addMessage(Message message)
        {
            if (message != null)
            {
                Logger.d(TAG, "%s: Adding to outgoing queue message: %s", name, message.toString());

                // Size is huge because this is a LinkedBlockingQueue, so don't worry about failure
                messageQueue.offer(message);
            }
        }

        public void start()
        {
            new Thread(this, "MessageConnection_Outgoing").start();
        }

        public void stop()
        {
            // This should be safe because we're racing only for it to get set
            if (myThread != null)
            {
                myThread.interrupt();
            }
        }

        @Override
        public void run()
        {
            myThread = Thread.currentThread();

            while (isOpen)
            {
                Message message = null;
                byte[] rawBytes = null;

                try
                {
                    message = messageQueue.take();
                }
                catch (InterruptedException e)
                {
                    // Don't do anything special here; message will be null and we'll loop again
                }

                // Serialize the message
                if (message != null)
                {
                    try
                    {
                        rawBytes = messageSerializer.serialize(message);
                    }
                    catch (IllegalArgumentException e)
                    {
                        if (isOpen)
                        {
                            Logger.e(TAG, "%s: Error serializing message: %s", name, e.getMessage());
                        }
                    }
                }

                // Send the serialized message
                if (rawBytes != null)
                {
                    try
                    {
                        Logger.d(TAG, "%s: Sending %d bytes for message %s", name, rawBytes.length,
                                message.toString());
                        frameDest.sendFrame(rawBytes);
                    }
                    catch (IOException e)
                    {
                        if (isOpen && listener != null)
                        {
                            listener.onSendError(e);
                        }
                    }
                }
            }

            myThread = null;
            messageQueue.clear();
        }
    }

    private class OutstandingMessage
    {
        public final Message message;
        public boolean isOutstanding;

        public OutstandingMessage(Message message)
        {
            this.message = message;
            isOutstanding = true;
        }

        public final Runnable expireRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (this)
                {
                    if (isOutstanding)
                    {
                        isOutstanding = false;
                        outstandingMessages.remove(message.getId());
                    }
                    else
                    {
                        return;
                    }
                }

                if (listener != null)
                {
                    listener.onNoMessageResponse(message);
                }
            }
        };
    }
}
