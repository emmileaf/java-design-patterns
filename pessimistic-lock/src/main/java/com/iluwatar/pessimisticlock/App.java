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
        BookRepository bookRepository = new BookRepository();
        // consider a repository of two book objects
        long book1Id = 1;
        long book2Id = 2;
        Book book1 = new Book();
        Book book2 = new Book();
        book1.setId(book1Id);
        book1.setTitle("The Hobbit");
        book2.setId(book2Id);
        bookRepository.add(book1); // adding a book with empty title and author
        LOGGER.info("A book with id {} and title {} was added to repository", book1.getId(), book1.getTitle());
        bookRepository.add(book2); // adding a book with empty title and author
        LOGGER.info("An empty book with id {} was added to repository", book2.getId());

        // set up a SessionManager to let user sessions access the book repository
        var sessionManager = new SessionManager(bookRepository);

        // Alice and Bob represent two concurrent user sessions
        var aliceSession = sessionManager.newSession("Alice");
        var bobSession = sessionManager.newSession("Bob");

        // Alice and Bob try to operate on books concurrently
        new Thread(() -> {
            Boolean written = sessionManager.write(aliceSession, book1Id, "Title", "Harry Potter");
            assert(written == true);
        }).start();

        new Thread(() -> {
            // read operation should fail to acquire lock
            String newTitle = sessionManager.read(bobSession, book1Id, "Title"); // should fail to acquire lock
            assert(newTitle == null);
        }).start();

        new Thread(() -> {
            // write operation should fail to acquire lock
            Boolean written = sessionManager.write(bobSession, book1Id, "Title", "Watership Down");
            assert(written == false);
        }).start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            LOGGER.info("Time stall interrupted");
        }

        new Thread(() -> {
            String newTitle = sessionManager.read(bobSession, book1Id, "Title"); // should succeed to read
            assert(newTitle == "Harry Potter");
        }).start();

    }
}
