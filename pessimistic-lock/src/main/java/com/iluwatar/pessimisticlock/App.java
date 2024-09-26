package com.iluwatar.pessimisticlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    /**
     * Added, CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1307
     * Program entry point.
     *
     * @param args command line args
     */
    public static void main(String[] args) {

        // End-to-end test scenario below - see test suite for more examples

        // bookRepository represents a simple database
        BookRepository bookRepository = new BookRepository();
        // consider a repository of one book object
        long book1Id = 1;
        Book book1 = new Book();
        book1.setId(book1Id);
        book1.setTitle("The Hobbit");
        try {
            bookRepository.add(book1); // adding a book and pre-setting its title
            LOGGER.info("A book with id {} and title {} was added to repository", book1.getId(), book1.getTitle());
        } catch (BookException e) {
            LOGGER.error("UNEXPECTED: book with id {} already exists in repository", book1.getId());
        }

        // set up a SessionManager to let user sessions access the book repository
        SessionManager sessionManager = new SessionManager(bookRepository);

        // Alice and Bob represent two concurrent user sessions
        String aliceSession = sessionManager.newSession("Alice");
        String bobSession = sessionManager.newSession("Bob");
        String title = "Title";

        // Alice and Bob try to operate on books concurrently
        new Thread(() -> {
            LOGGER.info("Alice initiated WRITE operation on book {}.", book1.getId());
            try {
                sessionManager.write(aliceSession, book1Id, title, "Harry Potter");
                LOGGER.info("EXPECTED: Alice performed WRITE operation on book {}.", book1.getId());
            } catch (Exception e) {
                LOGGER.error("UNEXPECTED: Alice is unable to perform WRITE operation on book {}.", book1.getId());
            }
        }).start();

        new Thread(() -> {
            // read operation should fail to acquire lock
            try {
                Thread.sleep(1000);
                LOGGER.info("Bob initiated READ operation on book {}.", book1.getId());
                sessionManager.read(bobSession, book1Id, title);
                LOGGER.error("UNEXPECTED: Bob performed READ on book {}, but shouldn't be allowed.", book1.getId());
            } catch (LockException e1) {
                LOGGER.info("EXPECTED: Bob is unable to perform READ on book {} while Alice edits.", book1.getId());
            } catch (Exception e2) {
                LOGGER.error(e2.getMessage());
            }
        }).start();

        new Thread(() -> {
            // write operation should fail to acquire lock
            try {
                Thread.sleep(1000);
                LOGGER.info("Bob initiated WRITE operation on book {}.", book1.getId());
                sessionManager.write(bobSession, book1Id, title, "Watership Down");
                LOGGER.error("UNEXPECTED: Bob can perform WRITE on book {}, but shouldn't be allowed.", book1.getId());
            } catch (LockException e1) {
                LOGGER.info("EXPECTED: Bob is unable to perform WRITE on book {} while Alice edits.", book1.getId());
            } catch (Exception e2) {
                LOGGER.error(e2.getMessage());
            }
        }).start();

        new Thread(() -> {
            // read operation should now succeed
            try {
                Thread.sleep(2000);
                LOGGER.info("Bob initiated READ operation on book {}.", book1.getId());
                String newTitle = sessionManager.read(bobSession, book1Id, title); // should succeed to read
                if (newTitle == "Harry Potter") {
                    LOGGER.info("EXPECTED: Bob is able perform READ on book {} to fetch updated title.", book1.getId());
                } else {
                    LOGGER.error("UNEXPECTED: Bob did not fetch the updated title for book {}.", book1.getId());
                }
            } catch (Exception e) {
                LOGGER.error("UNEXPECTED: " + e.getMessage());
            }
        }).start();

    }
}
