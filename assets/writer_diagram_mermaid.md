sequenceDiagram
participant Client
participant Writer as SingleWriterImpl
participant State as SharedBufferState

    Client->>Writer: write(item)
    activate Writer

    Writer->>State: getWriteSequence()
    State-->>Writer: currentSeq

    Note over Writer: Compute index = currentSeq % capacity

    Writer->>State: put(currentSeq, item)

    Note over Writer: Write completes before sequence increment

    Writer->>State: incrementWriteSequence()

    deactivate Writer