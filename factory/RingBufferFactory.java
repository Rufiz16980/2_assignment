package factory;

import api.RingBufferReader;
import api.RingBufferWriter;
import core.SharedBufferState;
import impl.IndependentReaderImpl;
import impl.SingleWriterImpl;

public class RingBufferFactory {
    // static methods so no need to create new ring buffers
    public static <T> SharedBufferState<T> createSharedState(int capacity) {

        return new SharedBufferState<>(capacity);
    }

    public static <T> RingBufferWriter<T> createWriter(SharedBufferState<T> state) {
        return new SingleWriterImpl<>(state);
    }

    public static <T> RingBufferReader<T> createReader(SharedBufferState<T> state) {
        return new IndependentReaderImpl<>(state);
    }
}