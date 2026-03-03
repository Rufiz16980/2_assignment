package api;

import java.util.Optional;

public interface RingBufferReader<T> {
    Optional<T> read();
}