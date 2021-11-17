package com.iluwatar.pessimisticlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The SessionManager manages {@link Session} access to a {@link BookRepository} using exclusive read locks
 * Each user session requires lock to load record and perform any operation on Book objects from the repository
 */
public class SessionManager {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final Map<Long, String> locks;
    private final HashMap<String, Session> sessions;
    private final BookRepository bookRepo;
    private int sessionId;

    /**
     * Constructor to initialize a session manager around a {@link BookRepository}.
     * @param repo - Book repository to manage access for
     */
    public SessionManager(BookRepository repo) {
        this.bookRepo = repo;
        this.sessions = new HashMap<>();
        this.locks = new HashMap<>();
        this.sessionId = 0;
    }

    /**
     * Creates new {@link Session} that connects a given user to the repository.
     * @param user - Name of user who owns this session
     * @return assigned ID for the newly created session
     */
    public String newSession(String user) {
        String newId = String.valueOf(this.sessionId);
        Session newSession = new Session(user, newId);
        sessions.put(newId, newSession);
        this.sessionId += 1;
        return newId;
    }

    /**
     * Removes an existing {@link Session} and disconnect that user from the repository.
     * Releases all object locks still held by session, if any, without committing outstanding changes.
     * @param sessionId - Identifier of user session to remove
     */
    public void removeSession(String sessionId) {
        releaseAll(sessionId);
        sessions.remove(sessionId);
    }

    /**
     * Returns number of connected user sessions
     */
    public int numSessions() {
        return sessions.size();
    }

    /**
     * Executes a write request initiated by a given user session on a book.
     * @param sessionId - Identifier of user session performing the operation
     * @param bookId - Identifier of book object to performing the operation on
     * @param writeField - String specifying which field to update in the book
     * @param writeValue - String specifying what value to update field to
     */
    public void write(String sessionId, Long bookId, String writeField, String writeValue)
            throws LockException, BookException, IllegalArgumentException, InterruptedException {
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
            } catch (LockException e1) {
                LOGGER.info("Could not acquire lock on book {}.", bookId);
                throw e1;
            } catch (BookException e2) {
                LOGGER.info("Book {} not found.", bookId);
                throw e2;
            } catch (IllegalArgumentException e3) {
                LOGGER.info("{} is not an invalid field for book {}.", writeField, bookId);
                throw e3;
            } catch (InterruptedException e4) {
                LOGGER.info("Thread.sleep is interrupted for book {}.", bookId);
                throw e4;
            }
        } else {
            throw new IllegalArgumentException("Session " + sessionId + " is not found.");
        }
    }

    /**
     * Executes a read request initiated by a given user session on a book.
     * @param sessionId - Identifier of user session performing the operation
     * @param bookId - Identifier of book object to performing the operation on
     * @param readField - String specifying which field to read value from the book
     * @return string value fetched by operation
     */
    public String read(String sessionId, Long bookId, String readField)
            throws LockException, BookException, IllegalArgumentException {
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
            } catch (LockException e1) {
                LOGGER.info("Could not acquire lock on book {}.", bookId);
                throw e1;
            } catch (BookException e2) {
                LOGGER.info("Book {} not found.", bookId);
                throw e2;
            } catch (IllegalArgumentException e3) {
                LOGGER.info("{} is not an invalid field for book {}.", readField, bookId);
                throw e3;
            }
        } else {
            throw new IllegalArgumentException("Session " + sessionId + " is not found.");
        }
    }

    private boolean hasLock(Long lockable, String owner) {
        return (locks.get(lockable) != null && locks.get(lockable).equals(owner));
    }

    private void acquireLock(Long lockable, String owner) throws BookException, LockException {
        Session userSession = sessions.get(owner);
        if (userSession != null && !hasLock(lockable, owner)) {
            if (locks.containsKey(lockable)) {
                throw new LockException("Another user has lock on " + lockable);
            } else {
                locks.put(lockable, owner); // acquire lock, then load data
                userSession.checkoutBook(bookRepo.get(lockable));
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
        Session userSession = sessions.get(owner);
        if (userSession != null) {
            for (Long lockable: userSession.getLocks()) {
                releaseLock(lockable, owner);
            }
        }
    }
}
