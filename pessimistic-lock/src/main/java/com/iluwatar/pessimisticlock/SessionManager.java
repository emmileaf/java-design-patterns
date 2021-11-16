package com.iluwatar.pessimisticlock;

import java.util.HashMap;
import java.util.Map;

// Managers user sessions' access to a book repo using exclusive read locks: session requires lock to load record
public class SessionManager {
    private final Map<Long, String> locks;
    private final HashMap<String, Session> sessions;
    private final BookRepository bookRepo;
    private int sessionId;

    public SessionManager(BookRepository repo) {
        this.bookRepo = repo;
        this.sessions = new HashMap<>();
        this.locks = new HashMap<>();
        this.sessionId = 0;
    }

    private boolean hasLock(Long lockable, String owner) {
        return (locks.get(lockable) != null && locks.get(lockable).equals(owner));
    }

    private void acquireLock(Long lockable, String owner) throws BookNotFoundException, LockException {
        Session userSession = sessions.get(owner);
        if (userSession != null && !hasLock(lockable, owner)) {
            if (locks.containsKey(lockable)) {
                throw new LockException("Another user has lock on " + lockable);
            } else {
                locks.put(lockable, owner); // acquire lock, then load data
                userSession.checkoutBook(lockable, bookRepo.get(lockable));
            }
        }
    }

    private void releaseLock(Long lockable, String owner) {
        Session userSession = sessions.get(owner);
        if (userSession != null && hasLock(lockable, owner)) {
            userSession.releaseBook(lockable);
            locks.remove(lockable);
        }
    }

    private void releaseAll(String owner) {
        // TODO: this iteration is very slow and not scalable - optimize using session's hashmap of books?
        for (Map.Entry<Long, String> entry : locks.entrySet()) {
            if (entry.getValue().equals(owner)) {
                locks.remove(entry.getKey());
            }
        }
    }

    public String newSession(String user) {
        String newId = String.valueOf(this.sessionId);
        Session newSession = new Session(user, newId);
        sessions.put(newId, newSession);
        this.sessionId += 1;
        return newId;
    }

    public void removeSession(String sessionId) {
        // TODO: remove all locks currently held by session
    }

    public boolean write(String sessionId, Long bookId, String writeField, String writeValue) {
        // Acquire read lock on book, modify entries, then commit changes to book repo and release lock
        Session userSession = sessions.get(sessionId);
        if (userSession != null) {
            try {
                // acquire lock
                acquireLock(bookId, sessionId);
                // perform operations and edit record; 2s time stall to imitate a computationally heavy operation
                Thread.sleep(2000);
                Book updated = userSession.editBook(bookId, writeField, writeValue);
                // commit changes
                bookRepo.update(updated);
                // release lock
                releaseLock(bookId, sessionId);
                return true;
            } catch (LockException | BookNotFoundException e1) {
                // issue with acquiring lock
                return false;
            } catch (IllegalArgumentException e2) {
                // editBook is given an invalid write field
                return false;
            } catch (InterruptedException e3) {
                // Thread.sleep interrupted
                return false;
            }
        }
        return false;
    }

    public String read(String sessionId, Long bookId, String readField) {
        // Acquire read lock on book, return field value read, then release lock
        Session userSession = sessions.get(sessionId);
        if (userSession != null) {
            try {
                // acquire lock
                acquireLock(bookId, sessionId);
                // read field value from record
                String result = userSession.readBook(bookId, readField);
                // release lock and return result
                releaseLock(bookId, sessionId);
                return result;
            } catch (LockException | BookNotFoundException e1) {
                return null;
            } catch (IllegalArgumentException e2) {
                // readBook is given an invalid write field
                return null;
            }
        }
        return null;
    }
}
