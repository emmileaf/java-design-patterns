package com.iluwatar.pessimisticlock;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Session is a package-private class that represents a user session (with name and id assigned)
 * and holds a collection of books that it currently has access to (lock acquired).
 * Session creation and management is handled by the {@link SessionManager}.
 */
class Session {
    private final String user;
    private final String id;
    private final Map<Long, Book> books; // a collection of books where session has acquired lock

    /** Constructor to initialize a session given user name and ID.
     *  @param user  - Name of the user owning this session
     *  @param id    - Session identifier string
     */
    Session(String user, String id) {
        this.user = user;
        this.id = id;
        this.books = new HashMap<>();
    }

    /** Returns the session ID. */
    String getId() {
        return this.id;
    }

    /** Returns the session user name. */
    String getUser() {
        return this.user;
    }

    /** Returns the set of lockable IDs currently held by the session. */
    Set<Long> getLocks() {
        return this.books.keySet();
    }

    /**
     * Adds a book object to the session's collection of lock-obtained books.
     * @param book  - Book object from repository, with loack already obtained
     */
     void checkoutBook(Book book) {
         if (!books.containsKey(book.getId())) {
            books.put(book.getId(), book);
        }
    }

    /**
     * Removes a book object from the session's collection of lock-obtained books.
     * Called after changes are committed to the book repository, and before releasing its lock.
     * @param bookId  - Identifier for Book object to remove from collection
     */
    void releaseBook(Long bookId) {
        if (books.containsKey(bookId)) {
            books.remove(bookId);
        }
    }

    /**
     * Performs write operation to modify the given field for a book.
     * @param bookId  - Identifier for Book object to modify in collection
     * @param field   - Specifies which field (Title/Author) in a Book object to edit.
     * @param value   - Specifies the desired value for the given field.
     * @return the modified Book object.
     */
    Book editBook(Long bookId, String field, String value) throws IllegalArgumentException {
        Book existing = books.get(bookId);
        if (field == "Author") {
            existing.setAuthor(value);
        } else if (field == "Title") {
            existing.setTitle(value);
        } else {
            throw new IllegalArgumentException(field + " is not a valid Book field.");
        }
        books.put(bookId, existing);
        return existing;
    }

    /**
     * Performs read operation to fetch the given field for a book.
     * @param bookId  - Identifier for Book object to access from collection
     * @param field   - Specifies which field (Title/Author) in a Book object to fetch.
     * @return the value of the fetched field.
     */
    String readBook(Long bookId, String field) throws IllegalArgumentException {
        Book existing = books.get(bookId);
        if (field == "Author") {
            return existing.getAuthor();
        } else if (field == "Title") {
            return existing.getTitle();
        } else {
            throw new IllegalArgumentException(field + " is not a valid Book field.");
        }
    }

}

