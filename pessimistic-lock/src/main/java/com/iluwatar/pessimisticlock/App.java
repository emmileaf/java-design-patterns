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
            BookDuplicateException {

        // TODO: End-to-end test scenario below - incorporate into test suite later

        // bookRepository represents a simple database
        BookRepository bookRepository = new BookRepository();
        // consider a repository of one book object
        long book1Id = 1;
        Book book1 = new Book();
        book1.setId(book1Id);
        book1.setTitle("The Hobbit");
        bookRepository.add(book1); // adding a book and pre-setting its title
        LOGGER.info("A book with id {} and title {} was added to repository", book1.getId(), book1.getTitle());

        // set up a SessionManager to let user sessions access the book repository
        var sessionManager = new SessionManager(bookRepository);

        // Alice and Bob represent two concurrent user sessions
        var aliceSession = sessionManager.newSession("Alice");
        var bobSession = sessionManager.newSession("Bob");

        // Alice and Bob try to operate on books concurrently
        new Thread(() -> {
            LOGGER.info("Alice initiated WRITE operation on book {}.", book1.getId());
            try {
                sessionManager.write(aliceSession, book1Id, "Title", "Harry Potter");
                LOGGER.info("Alice performed WRITE operation on book {}.", book1.getId());
            } catch (Exception e) {
                LOGGER.error("Alice is unable to perform WRITE operation on book {}.", book1.getId());
            }
        }).start();

        new Thread(() -> {
            // read operation should fail to acquire lock
            try {
                LOGGER.info("Bob initiated READ operation on book {}.", book1.getId());
                String newTitle = sessionManager.read(bobSession, book1Id, "Title");
                LOGGER.error("Bob performed READ on book {}, but shouldn't be allowed as Alice edits.", book1.getId());
            } catch (LockException e1) {
                LOGGER.info("Bob is unable to perform READ on book {} while Alice is editing.", book1.getId());
            } catch (Exception e2) {
                LOGGER.error(e2.getMessage());
            }
        }).start();

        new Thread(() -> {
            // write operation should fail to acquire lock
            try {
                LOGGER.info("Bob initiated WRITE operation on book {}.", book1.getId());
                sessionManager.write(bobSession, book1Id, "Title", "Watership Down");
                LOGGER.error("Bob can perform WRITE on book {}, but should not be while Alice edits.", book1.getId());
            } catch (LockException e1) {
                LOGGER.info("Bob is unable to perform WRITE on book {} while Alice is editing.", book1.getId());
            } catch (Exception e2) {
                LOGGER.error(e2.getMessage());
            }
        }).start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            LOGGER.error("Time stall of two seconds was interrupted");
        }

        new Thread(() -> {
            // read operation should now succeed
            try {
                LOGGER.info("Bob initiated READ operation on book {}.", book1.getId());
                String newTitle = sessionManager.read(bobSession, book1Id, "Title"); // should succeed to read
                if (newTitle == "Harry Potter") {
                    LOGGER.info("Bob is able perform READ on book {} and fetch the updated title.", book1.getId());
                } else {
                    LOGGER.error("Bob did not fetch the correct title for on book {} updated by Alice.", book1.getId());
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }).start();

    }
}
