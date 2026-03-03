package impl;

import api.RingBufferWriter;
import core.SharedBufferState;

public class SingleWriterImpl<T> implements RingBufferWriter<T> {
    private final SharedBufferState<T> state;

    public SingleWriterImpl(SharedBufferState<T> state) {
        this.state = state;
    }

    @Override
    public void write(T item) {
        long currentSeq = state.getWriteSequence();

        // write the data to the array
        state.put(currentSeq, item);

        state.incrementWriteSequence();
    }
}