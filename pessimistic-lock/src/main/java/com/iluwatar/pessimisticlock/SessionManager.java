package com.iluwatar.pessimisticlock;

import java.util.HashMap;

public class SessionManager {
    private HashMap<String, Session> sessions;
    private BookRepository bookRepo;
    private int sessionId = 0;

    public SessionManager(BookRepository repo) {
        this.bookRepo = repo;
        this.sessions = new HashMap<>();
    }

    public String newSession(String user, String) {
        String newId = String.valueOf(this.sessionId);
        Session newSession = new Session(user, newId);
        sessions.put(newId, newSession);
        this.sessionId += 1;
        return newId;
    }

    public boolean checkout(String sessionId, String bookId) {
        // user session acquiring read/write lock on book, return true if successful
        Session userSession = sessions.get(sessionId);
        if (userSession != null) {
            try {
                userSession.checkout(bookId);
                return true;
            } catch (LockException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean write(String sessionId, String bookId, String writeField, String writeValue) {
        // user session should already have acquired read/write lock on book
        // if so, modify and commit change to book repository, then release lock
        Session userSession = sessions.get(sessionId);
        if (userSession != null && userSession.hasBook(bookId)) {
            try {
                Book updated = userSession.editBook(bookId, writeField, writeValue);
                userSession.releaseLock(bookId); // TODO: release lock first, or commit changes to repo first?
                bookRepo.update(updated);
                return true;
            } catch (IllegalArgumentException e1) {
                // writeField not a valid field to update
                return false;
            } catch (BookNotFoundException e2) {
                // book does not exist in repo when committing changes
                return false;
            }
        } else {
            return false;
        }
    }

    public Book read(String sessionId, String bookId) {
        // user session should already have acquired read/write lock on book
        // if so, fetch value from book repository, then release lock
        Session userSession = sessions.get(sessionId);
        if (userSession != null && userSession.hasBook(bookId)) {
            Book read = userSession.getBook(bookId);
            userSession.releaseLock(bookId);
            return read;
        } else {
            return null;
        }
    }
}
