package cards.herscher.cardmaster.comm;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * This class is not thread safe.
 * 
 */
public class Frame
{
    public final static int MAX_FRAME_LENGTH = 2048;
    public final static byte START_BYTE = 0x40;
    public final static byte END_BYTE = 0x41;
    public final static byte ESCAPE_BYTE = (byte) 0x80;

    public interface Listener
    {
        public void onFrameAvailable(byte[] frameBytes);
        
        public void onInvalidFrame();
    }

    private enum ParseState
    {
        SYNC_TO_START, UNESCAPING, NORMAL
    }

    private final Listener listener;
    private final ByteBuffer rxBuffer;
    private final byte[] createWorkingBuffer;
    private ParseState parseState;

    public Frame(Listener listener)
    {
        this.listener = listener;
        parseState = ParseState.SYNC_TO_START;

        // *2 for worst-case escaping
        rxBuffer = ByteBuffer.allocate(MAX_FRAME_LENGTH * 2);
        createWorkingBuffer = new byte[MAX_FRAME_LENGTH * 2];
    }

    public void parse(byte[] rawBytes)
    {
        if (rawBytes == null)
        {
            throw new IllegalArgumentException();
        }

        if (rawBytes.length > 0)
        {
            int startIndex = 0;

            while (startIndex >= 0)
            {
                startIndex = subParse(rawBytes, startIndex);
            }
        }
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

    private int subParse(byte[] rawBytes, int start)
    {
        byte currentByte;

        for (int i = start; i < rawBytes.length; i++)
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
                        byte[] frameBytes = Arrays.copyOf(rxBuffer.array(), rxBuffer.position());
                        resetParseState();
                        if (listener != null)
                        {
                            listener.onFrameAvailable(frameBytes);
                        }
                        return i;
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

        return -1;
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
        }
    }
}
