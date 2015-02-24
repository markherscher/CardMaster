package cards.herscher.comm.message;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * This class is not thread-safe.
 * 
 * @author MarkHerscher
 * 
 */
public class KryoMessageSerializer implements MessageSerializer
{
    private final Kryo kryo;

    public KryoMessageSerializer()
    {
        kryo = new Kryo();

        kryo.register(ResponseMessage.class);
    }

    @SuppressWarnings("rawtypes")
    public void registerMessageClass(Class type)
    {
        kryo.register(type);
    }

    public Message deserialize(byte[] rawBytes) throws KryoException
    {
        if (rawBytes == null)
        {
            throw new IllegalArgumentException("rawBytes was null");
        }

        Object obj;
        Input input = new Input(rawBytes);

        try
        {
            obj = kryo.readClassAndObject(input);
        }
        catch (KryoException e)
        {
            throw new IllegalArgumentException(e.getMessage());
        }
        finally
        {
            input.close();
        }

        if (obj instanceof Message)
        {
            return (Message) obj;
        }
        else
        {
            throw new IllegalArgumentException("resulting object type was not Message");
        }
    }

    public byte[] serialize(Message message) throws KryoException
    {
        if (message == null)
        {
            throw new IllegalArgumentException("message was null");
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Output output = null;

        try
        {
            output = new Output(byteStream);
            kryo.writeClassAndObject(output, message);
        }
        catch (KryoException e)
        {
            throw new IllegalArgumentException(e.getMessage());
        }
        finally
        {
            if (output != null)
            {
                output.close();
            }
        }

        return byteStream.toByteArray();
    }
}
