package api;

public interface RingBufferWriter<T> {
    void write(T item);
}