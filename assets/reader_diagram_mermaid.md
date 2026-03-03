sequenceDiagram
participant Client
participant Reader as IndependentReaderImpl
participant State as SharedBufferState

    Client->>Reader: read()
    activate Reader

    loop while true (Retry Loop)
        Reader->>State: getWriteSequence()
        State-->>Reader: globalSeq

        break when Buffer Empty
            Reader-->>Client: Optional.empty()
        end

        opt when Slow Reader (Lapped by Writer)
            Note over Reader: nextReadSeq = globalSeq - capacity
        end

        Reader->>State: get(nextReadSeq)
        State-->>Reader: item

        Reader->>State: getWriteSequence()
        State-->>Reader: newGlobalSeq

        alt Overwrite Detected Mid-Read
            Note over Reader: Discard item (continue loop)
        else Safe Read
            Note over Reader: nextReadSeq++
            Reader-->>Client: Optional.of(item)
        end
    end
    deactivate Reader