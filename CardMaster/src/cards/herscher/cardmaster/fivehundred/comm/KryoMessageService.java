package cards.herscher.cardmaster.fivehundred.comm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;

import android.os.Handler;

import cards.herscher.cardmaster.Logger;

public class KryoMessageService
{
    public interface Listener
    {
        // TODO: need return type to know what to send in ACK
        public void onMessageReceived(Message message);

        public void onStartError(IOException e);

        public void onReadError(IOException e);

        public void onStarted();
    }

    private final static String TAG = "KryoMessageService";

    private final Object startLock;
    private final Listener listener;
    private final Handler handler;
    private final FrameSource frameSource;
    private final Kryo kryo;
    private boolean isRunning;

    public KryoMessageService(FrameSource frameSource, Handler handler, Listener listener)
    {
        if (frameSource == null || handler == null)
        {
            throw new IllegalArgumentException();
        }

        this.frameSource = frameSource;
        this.listener = listener;
        this.handler = handler;
        startLock = new Object();
        kryo = new Kryo();

        kryo.register(HandshakeMessage.class);
    }

    public void start()
    {
        synchronized (startLock)
        {
            if (isRunning)
            {
                throw new IllegalStateException("already started");
            }

            isRunning = true;
            new Thread(new ReadSocketRunnable(), "ReadMessageThread").start();
        }
    }

    private void handleFrameBytes(byte[] frameBytes)
    {
        Object someObject = null;

        try
        {
            Input input = new Input(new ByteArrayInputStream(frameBytes));
            someObject = kryo.readClassAndObject(input);
        }
        catch (KryoException e)
        {
            Logger.e(TAG, "Failed to deserialize: %s", e.getMessage());
        }

        if (someObject instanceof Message)
        {
            final Message message = (Message) someObject;

            if (listener != null)
            {
                handler.post(new Runnable()
                {
                    public void run()
                    {
                        listener.onMessageReceived(message);
                    }
                });
            }
        }
        else if (someObject != null)
        {
            Logger.w(TAG, "Received object not of type Message (%s)", someObject.toString());
        }
    }

    private class ReadSocketRunnable implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                frameSource.init();
            }
            catch (final IOException e)
            {
                if (listener != null)
                {
                    handler.post(new Runnable()
                    {
                        public void run()
                        {
                            listener.onStartError(e);
                        }
                    });
                }
                return;
            }

            while (true)
            {
                try
                {
                    List<byte[]> frames = frameSource.readFrames();

                    for (byte[] frameBytes : frames)
                    {
                        handleFrameBytes(frameBytes);
                    }
                }
                catch (final IOException e)
                {
                    if (listener != null)
                    {
                        handler.post(new Runnable()
                        {
                            public void run()
                            {
                                listener.onReadError(e);
                            }
                        });
                    }
                }
            }
        }
    }

}
