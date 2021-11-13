package com.iluwatar.pessimisticlock;

import java.util.HashMap;

public class Session {
    private String user;
    private String id;
    private BookRepository repo; // TODO: how to access repository via session; is this pass by reference?
    private HashMap<String, Book> books; // a collection of books where session has acquired lock

    public Session(String user, String id, BookRepository repo) {
        this.user = user;
        this.id = id;
        this.repo = repo;
    }
    public String getId() {
        return this.id;
    }

    public boolean checkOut(String bookId) throws LockException {
        // @TODO: try to acquire read lock on book
    }

    public void editTitle(String bookId, String title) {
        // @TODO: should this modify in-place or return edited copy
    }

    public void editAuthor(String bookId, String author) {
        // @TODO: should this modify in-place or return edited copy
    }

    public boolean commitReturn(Book book) {
        // @TODO: commit changes made to book to repo, and release lock
    }

}

