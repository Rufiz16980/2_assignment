```mermaid
sequenceDiagram
autonumber
participant Client
participant Writer as SingleWriterImpl
participant Reader as IndependentReaderImpl
participant State as SharedBufferState

    %% --- WRITE SEQUENCE ---
    Client->>Writer: write(item)
    activate Writer
    Writer->>State: getWriteSequence()
    activate State
    State-->>Writer: currentSeq
    deactivate State
    
    Writer->>State: put(currentSeq, item)
    Writer->>State: incrementWriteSequence()
    deactivate Writer
   
    %% --- READ SEQUENCE ---
    Client->>Reader: read()
    activate Reader
    
    loop Optimistic Concurrency Check
        Reader->>State: getWriteSequence()
        State-->>Reader: globalSeq
        
        alt Buffer Empty
            Reader-->>Client: Optional.empty()
        else Slow Reader (Lapped)
            Note over Reader,State: If (globalSeq - nextReadSeq > capacity)
            Reader->>Reader: nextReadSeq = globalSeq - capacity
        end
        
        Reader->>State: get(nextReadSeq)
        State-->>Reader: item
        
        Reader->>State: getWriteSequence()
        State-->>Reader: newGlobalSeq
        
        Note over Reader,State: Did writer overwrite while fetching?
        opt No Overwrite
            Reader->>Reader: nextReadSeq++
            Reader-->>Client: Optional.of(item)
        end
    end
    deactivate Reader