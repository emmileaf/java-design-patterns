package com.iluwatar.pessimisticlock;

import java.util.HashMap;

class Session {
    private String user;
    private String id;
    private HashMap<Long, Book> books; // a collection of books where session has acquired lock

    public Session(String user, String id) {
        this.user = user;
        this.id = id;
    }
    public String getId() {
        return this.id;
    }

    public boolean hasBook(Long bookId) {
        return books.containsKey(bookId);
    }

    private Book getBook(Long bookId) {
        return books.get(bookId);
    }

    public void checkoutBook(Long bookId, Book book) {
        // After acquiring read lock on book
        if (!books.containsKey(bookId)) {
            books.put(bookId, book);
        }
    }

    public void releaseBook(Long bookId) {
        // Before releasing read lock on book, fetch updated Book to commit to book repo
        if (books.containsKey(bookId)) {
            books.remove(bookId);
        }
    }

//    public Book returnBook(Long bookId) throws BookNotFoundException {
//        // Before releasing read lock on book, fetch updated Book to commit to book repo
//        Book updated;
//        if (books.containsKey(bookId)) {
//            updated = books.get(bookId);
//            books.remove(bookId);
//            return updated;
//        } else {
//            throw new BookNotFoundException("Session " + id + " does not have access to Book " + bookId.toString());
//        }
//    }

    public Book editBook(Long bookId, String field, String value) throws IllegalArgumentException {
        // @TODO: return edited copy
        Book existing = books.get(bookId);
        if (field == "Author") {
            existing.setAuthor(value);
        } else if (field == "Title") {
            existing.setTitle(value);
        } else {
            throw new IllegalArgumentException(field + " is not a valid Book field.");
        }
        return existing;
    }

    public String readBook(Long bookId, String field) {
        // @TODO: return value of field
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

