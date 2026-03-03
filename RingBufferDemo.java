import api.RingBufferReader;
import api.RingBufferWriter;
import core.SharedBufferState;
import factory.RingBufferFactory;

import java.util.Optional;

public class RingBufferDemo {
    public static void main(String[] args) {

        // create a ring buffer of size 5 and a writer
        SharedBufferState<String> state = RingBufferFactory.createSharedState(5);
        RingBufferWriter<String> writer = RingBufferFactory.createWriter(state);

        RingBufferReader<String> fastReader = RingBufferFactory.createReader(state);
        RingBufferReader<String> slowReader = RingBufferFactory.createReader(state);

        writer.write("A");
        writer.write("B");
        writer.write("C");

        System.out.println("Fast Reader reads: " + fastReader.read().orElse("Empty")); // A
        System.out.println("Fast Reader reads: " + fastReader.read().orElse("Empty")); // B

        // Overwrite data
        writer.write("D");
        writer.write("E");
        writer.write("F");
        writer.write("G");

        // At this point, total written is 7, array capacity is 5,
        // the oldest data point is at slot 2 - "C" so C is going to be read
        // slowReader is still looking for slot 0 "A"

        Optional<String> slowResult = slowReader.read();
        System.out.println("Slow Reader reads: " + slowResult.orElse("Empty") + " (A and B are skipped)");
    }
}