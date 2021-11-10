package com.iluwatar.pessimisticlock;

import org.mockito.internal.configuration.plugins.Plugins;

public interface LockManager {
    // Lock manager for exclusive read locks

    public void acquire(Long lockable, String owner) throws Exception;

    public void release(Long lockable, String owner);

    public void releaseAll(String owner);
}
