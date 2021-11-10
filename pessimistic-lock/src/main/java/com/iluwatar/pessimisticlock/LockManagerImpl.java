package com.iluwatar.pessimisticlock;

import java.util.HashMap;
import java.util.Map;

// Note: chapter in book walks through an example implementation using DB table
// try to implement alternative using a data structure and in-memory hash table
public class LockManagerImpl implements LockManager {
    private final Map<Long, String> locks = new HashMap<Long, String>();
    public void acquire(Long lockable, String owner) throws Exception {
        // @TODO: implement
        if (!hasLock(lockable, owner)) {
            // owner needs to acquire lock
            if (locks.containsKey(lockable)) {
                // object is locked, throw exception
                throw new Exception("lock cannot be obtained on " + lockable);
            } else {
                // acquire lock and add record to table
                locks.put(lockable, owner);
            }

        }
    }

    public void release(Long lockable, String owner) {
        // @TODO: implement
        if (hasLock(lockable, owner)) {
            locks.remove(lockable);
        }
    }

    public void releaseAll(String owner) {
        // @TODO: implement
        // Note: this iteration is very slow and not scalable - optimize later
        for (Map.Entry<Long, String> entry : locks.entrySet()) {
            if (entry.getValue().equals(owner)) {
                locks.remove(entry.getKey());
            }
        }
    }

    private boolean hasLock(Long lockable, String owner) {
        // @TODO: implement
        return (locks.get(lockable).equals(owner));
    }

}
