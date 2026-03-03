package impl;

import api.RingBufferReader;
import core.SharedBufferState;
import java.util.Optional;

public class IndependentReaderImpl<T> implements RingBufferReader<T> {
    private final SharedBufferState<T> state;
     // slot index alone does not tell how old is data so we need counter
    private long nextReadSequence;

    public IndependentReaderImpl(SharedBufferState<T> state) {
        this.state = state;
        // Start reading from wherever the writer currently is
        this.nextReadSequence = state.getWriteSequence();
    }

    @Override
    public Optional<T> read() {
        while (true) {
            long globalSeq = state.getWriteSequence();

            // Buffer is empty or reader is fully caught up
            if (nextReadSequence == globalSeq) {
                return Optional.empty();
            }

            //  Slow Reader
            if (globalSeq - nextReadSequence > state.getCapacity()) {
                // go to the oldest available  data
                nextReadSequence = globalSeq - state.getCapacity();
            }

            // actual read
            T item = state.get(nextReadSequence);

            // Mid-Read Overwrite ( this is why we have loop basically)
            if (state.getWriteSequence() - nextReadSequence > state.getCapacity()) {
                // The data we just grabbed is invalid, loop again
                continue;
            }

            // go further
            nextReadSequence++;
            return Optional.ofNullable(item); // basically to avoid null problem
        }
    }
}