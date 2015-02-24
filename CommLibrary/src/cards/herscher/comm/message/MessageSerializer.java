package cards.herscher.comm.message;

public interface MessageSerializer
{
    /**
     * Serializes the specified {@link Message} to bytes.
     * 
     * @param message
     *            the {@code Message} to serialize
     * @return the bytes
     * @throws IllegalArgumentException
     *             if the serialization failed
     */
    public byte[] serialize(Message message);

    /**
     * Deserializes the specified bytes to a {@link Message}.
     * 
     * @param rawBytes
     *            bytes to deserialize
     * @return the new {@code Message}
     * @throws IllegalArgumentException
     *             if the deserialization failed
     */
    public Message deserialize(byte[] rawBytes);
}
