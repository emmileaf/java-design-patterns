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

        // bookRepository imitates a simple database
        var bookRepository = new BookRepository();

        // aliceSession and bobSession imitate two concurrent user sessions
        final var aliceSession = new Session("Alice", "1", bookRepository);
        final var bobSession = new Session("Bob", "2", bookRepository);

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

        // Alice and Bob try to check out books concurrently
        aliceSession.checkOut(book1Id); // should succeed, acquired lock
        bobSession.checkOut(book2Id); // should succeed, acquired lock

        try {
            aliceSession.checkOut(book2Id); // should fail, bobSession has object lock
        } catch (LockException e) {
            LOGGER.info("Exception: {}", e.getMessage());
        }

        // Alice edits a book which Bob tries to check out, during and after the edit

        aliceSession.editTitle(1, "Kama Sutra"); // Alice has updated book title

        try {
            bobSession.checkOut(book1Id); // should fail
        } catch (LockException e) {
            LOGGER.info("Exception: {}", e.getMessage());
        }

        // TODO: aliceSession should commit changes to bookRepository and "return" book (releases lock)
        bobSession.checkOut(book1Id); // should succeed after Alice commits, TODO: check that book has updated title

    }
}
