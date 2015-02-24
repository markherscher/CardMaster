package cards.herscher.comm;


import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * Thread-safety: {@link #create(byte[])} and {@link #parse(byte[])} can be safely executed
 * concurrently, but multiple concurrent calls to the same method is not safe.
 * 
 */
public class Frame
{
    public final static int MAX_FRAME_LENGTH = 1024;
    public final static byte START_BYTE = 0x40;
    public final static byte END_BYTE = 0x41;
    public final static byte ESCAPE_BYTE = (byte) 0x80;

    public interface Listener
    {
        /**
         * Fired when a call to {@link Frame#parse(byte[])} detects bytes that are invalid for the
         * frame format. Parsing will continue without error, so this just a notification. This is
         * fired using the thread that called {@code parse()}, so do not call {@code parse()} from
         * it.
         */
        public void onInvalidFrame();

        /**
         * Fired when bytes are discarded because the end of a frame was not seen before the
         * {@link Frame#MAX_FRAME_LENGTH} was reached. This is fired using the thread that called
         * {@code parse()}, so do not call {@code parse()} from it.
         */
        public void onBytesDiscarded();
    }

    private enum ParseState
    {
        SYNC_TO_START, UNESCAPING, NORMAL
    }

    private final Listener listener;
    private final ByteBuffer rxBuffer;
    private final byte[] createWorkingBuffer;
    private final Queue<byte[]> completeFrames;
    private ParseState parseState;

    public Frame()
    {
        this(null);
    }

    public Frame(Listener listener)
    {
        this.listener = listener;
        parseState = ParseState.SYNC_TO_START;
        completeFrames = new ArrayDeque<byte[]>();
        rxBuffer = ByteBuffer.allocate(MAX_FRAME_LENGTH);

        // *2 for worst-case escaping
        createWorkingBuffer = new byte[MAX_FRAME_LENGTH * 2];
    }

    public boolean parse(byte[] rawBytes)
    {
        return parse(rawBytes, rawBytes.length);
    }

    public boolean parse(byte[] rawBytes, int length)
    {
        if (rawBytes == null)
        {
            throw new IllegalArgumentException("rawBytes cannot be null");
        }

        if (length < 0 || length > rawBytes.length)
        {
            throw new IllegalArgumentException("length is invalid");
        }

        byte currentByte;
        boolean foundFrame = false;

        for (int i = 0; i < length; i++)
        {
            currentByte = rawBytes[i];

            switch (parseState)
            {
                case SYNC_TO_START:
                    // Search until the start byte is found
                    if (currentByte == START_BYTE)
                    {
                        parseState = ParseState.NORMAL;
                    }
                    break;

                case UNESCAPING:
                    // Unescape the byte and store it
                    currentByte = (byte) ~currentByte;
                    parseState = ParseState.NORMAL;
                    storeParseByte(currentByte);
                    break;

                case NORMAL:
                    if (currentByte == ESCAPE_BYTE)
                    {
                        // Next byte should be unescaped
                        parseState = ParseState.UNESCAPING;
                    }
                    else if (currentByte == END_BYTE)
                    {
                        // This is the end of the frame
                        foundFrame = true;
                        byte[] frameBytes = Arrays.copyOf(rxBuffer.array(), rxBuffer.position());
                        completeFrames.add(frameBytes);
                        resetParseState();
                    }
                    else if (currentByte == START_BYTE)
                    {
                        if (listener != null)
                        {
                            listener.onInvalidFrame();
                        }

                        // We found an unexpected start byte. This is an error. Discard it all.
                        resetParseState();
                    }
                    else
                    {
                        storeParseByte(currentByte);
                    }
                    break;
            }
        }

        return foundFrame;
    }

    public byte[] getNextReceivedFrame()
    {
        return completeFrames.poll();
    }
    
    public boolean isFrameAvailable()
    {
        return completeFrames.peek() != null;
    }

    public byte[] create(byte[] rawBytes)
    {
        if (rawBytes == null || rawBytes.length > MAX_FRAME_LENGTH)
        {
            throw new IllegalArgumentException();
        }

        int index = 0;

        createWorkingBuffer[index++] = START_BYTE;

        for (byte b : rawBytes)
        {
            switch (b)
            {
                case START_BYTE:
                case END_BYTE:
                case ESCAPE_BYTE:
                    createWorkingBuffer[index++] = ESCAPE_BYTE;
                    createWorkingBuffer[index++] = (byte) ~b;
                    break;

                default:
                    createWorkingBuffer[index++] = b;
                    break;
            }
        }

        createWorkingBuffer[index++] = END_BYTE;

        return Arrays.copyOf(createWorkingBuffer, index);
    }

    private void resetParseState()
    {
        parseState = ParseState.SYNC_TO_START;
        rxBuffer.position(0);
    }

    private void storeParseByte(byte b)
    {
        // Normal data byte, so store it
        if (rxBuffer.hasRemaining())
        {
            rxBuffer.put(b);
        }
        else
        {
            // Frame is too long; discard it all
            resetParseState();

            if (listener != null)
            {
                listener.onBytesDiscarded();
            }
        }
    }
}
