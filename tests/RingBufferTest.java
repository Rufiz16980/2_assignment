package tests;

import api.RingBufferReader;
import api.RingBufferWriter;
import core.SharedBufferState;
import factory.RingBufferFactory;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RingBufferTest {

    @Test
    void testBasicWriteAndRead() {
        SharedBufferState<Integer> state = RingBufferFactory.createSharedState(5);
        RingBufferWriter<Integer> writer = RingBufferFactory.createWriter(state);
        RingBufferReader<Integer> reader = RingBufferFactory.createReader(state);

        writer.write(42);

        Optional<Integer> result = reader.read();
        assertTrue(result.isPresent(), "Reader should find the written item");
        assertEquals(42, result.get());

        // Should be empty on next read
        assertFalse(reader.read().isPresent(), "Reader should find nothing after consuming the item");
    }

    @Test
    void testIndependentReaders() {
        SharedBufferState<String> state = RingBufferFactory.createSharedState(5);
        RingBufferWriter<String> writer = RingBufferFactory.createWriter(state);

        RingBufferReader<String> reader1 = RingBufferFactory.createReader(state);
        RingBufferReader<String> reader2 = RingBufferFactory.createReader(state);

        writer.write("A");
        writer.write("B");

        // Reader 1 reads one item
        assertEquals("A", reader1.read().orElse("Empty"));

        // Reader 2 reads both items independently from the beginning
        assertEquals("A", reader2.read().orElse("Empty"));
        assertEquals("B", reader2.read().orElse("Empty"));

        // Reader 1 picks up exactly where it left off
        assertEquals("B", reader1.read().orElse("Empty"));
    }

    @Test
    void testSlowReaderOvertakenByWriter() {
        int capacity = 3;
        SharedBufferState<Integer> state = RingBufferFactory.createSharedState(capacity);
        RingBufferWriter<Integer> writer = RingBufferFactory.createWriter(state);
        RingBufferReader<Integer> slowReader = RingBufferFactory.createReader(state);

        // Write 5 items into a buffer of size 3.
        // This will overwrite items 1 and 2.
        writer.write(1);
        writer.write(2);
        writer.write(3);
        writer.write(4);
        writer.write(5);

        // The oldest surviving data in the buffer is now 3.
        Optional<Integer> result = slowReader.read();

        assertTrue(result.isPresent());
        assertEquals(3, result.get(), "Slow reader should realize it was lapped and jump to the oldest surviving data (3)");

        // It should continue reading the rest normally
        assertEquals(4, slowReader.read().get());
        assertEquals(5, slowReader.read().get());

        // Now it's caught up
        assertFalse(slowReader.read().isPresent());
    }

    @Test
    void testInvalidCapacityThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            RingBufferFactory.createSharedState(0);
        });

        assertTrue(exception.getMessage().contains("greater than 0"));
    }
}