package com.iluwatar.pessimisticlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    /**
     * Program entry point.
     *
     * @param args command line args
     */
    public static void main(String[] args) throws
            BookDuplicateException,
            BookNotFoundException,
            LockException {

        // TODO: End-to-end test scenario below - move to a different location later?

        // bookRepository represents a simple database
        var bookRepository = new BookRepository();
        // consider a repository of two book objects
        var book1Id = "1";
        var book2Id = "2";
        var book1 = new Book();
        var book2 = new Book();
        book1.setId(book1Id);
        book2.setId(book2Id);
        bookRepository.add(book1); // adding a book with empty title and author
        LOGGER.info("An empty book with id {} was added to repository", book1.getId());
        bookRepository.add(book2); // adding a book with empty title and author
        LOGGER.info("An empty book with id {} was added to repository", book2.getId());

        // set up a SessionManager to let user sessions access the book repository
        var sessionManager = new SessionManager(bookRepository);
        // Alice and Bob represent two concurrent user sessions
        var aliceSession = sessionManager.newSession("Alice");
        var bobSession = sessionManager.newSession("Bob");

        // Alice and Bob try to check out books concurrently
        var aliceBook1 = sessionManager.checkout(aliceSession, book1Id); // should succeed, acquired lock
        var bobBook2 =  sessionManager.checkout(bobSession, book2Id); // should succeed, acquired lock

        var aliceBook2 = sessionManager.checkout(aliceSession, book2Id); // should fail, bobSession has object lock


        // Alice edits a book which Bob tries to check out, during and after the edit
        sessionManager.write(aliceSession, book1Id, "Title", "Kama Sutra"); // Alice has updated book title

        var bobBook1 =  sessionManager.checkout(bobSession, book1Id); // should succeed, since Alice's write should release lock
        var book1updated = sessionManager.read(bobSession, book1Id); // book should have updated title

    }
}
