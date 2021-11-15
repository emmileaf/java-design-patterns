package com.iluwatar.pessimisticlock;

import java.util.HashMap;

class Session {
    private String user;
    private String id;
    private HashMap<String, Book> books; // a collection of books where session has acquired lock

    public Session(String user, String id) {
        this.user = user;
        this.id = id;
    }
    public String getId() {
        return this.id;
    }

    public boolean hasBook(String bookId) {
        return books.containsKey(bookId);
    }

    public Book getBook(String bookId) {
        return books.get(bookId);
    }

    public boolean checkout(String bookId) throws LockException {
        // @TODO: try to acquire read lock on book
    }

    public Book editBook(String bookId, String field, String value) throws IllegalArgumentException {
        // @TODO: return edited copy
    }

    public boolean releaseLock(String bookId) {
        // @TODO: release lock on book
    }

}

