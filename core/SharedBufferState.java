package core;


public class SharedBufferState<T> {
    private final Object[] data; // actual storage of items
    private final int capacity;

    // volatile ensures that when the writer updates this, all readers see the new value immediately
    private volatile long writeSequence; // how many items have been written

    public SharedBufferState(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = capacity;
        this.data = new Object[capacity];
        this.writeSequence = 0;
    }

    public int getCapacity() {
        return capacity;
    }

    public long getWriteSequence() {
        return writeSequence;
    }

    public void put(long sequence, T item) {

        data[(int) (sequence % capacity)] = item;
    }

    @SuppressWarnings("unchecked")
    public T get(long sequence) {
        return (T) data[(int) (sequence % capacity)];
    }

    public void incrementWriteSequence() {
        writeSequence++;
    }
}