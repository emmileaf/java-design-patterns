package com.iluwatar.pessimisticlock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for {@link SessionManager}
 */
class SessionManagerTest {

    private SessionManager manager;
    private String alice = "Alice";
    private String bob = "Bob";
    private String title = "Title";

    @BeforeEach
    void setUp() throws BookException {
        BookRepository bookRepo = new BookRepository();
        Book book1 = new Book();
        Book book2 = new Book();
        book1.setId((long) 1);
        book2.setId((long) 2);
        book1.setTitle("Book One");
        book2.setTitle("Book Two");
        bookRepo.add(book1);
        bookRepo.add(book2);
        manager = new SessionManager(bookRepo);
    }

    /** Test for the {@link SessionManager#newSession(String)} method */
    @Test
    void testNewSession() {
        String aliceSession = manager.newSession(alice);
        assertEquals("0", aliceSession);
        String bobSession = manager.newSession(bob);
        assertEquals("1", bobSession);
    }

    /** Test for the {@link SessionManager#removeSession(String)} method */
    @Test
    void testRemoveSession() {
        manager.newSession(alice);
        manager.newSession(bob);
        manager.removeSession("1");
        assertEquals(1, manager.numSessions());
    }

    /** Test for the {@link SessionManager#numSessions()}  method */
    @Test
    void testNumSessions() {
        assertEquals(0, manager.numSessions());
        manager.newSession(alice);
        manager.newSession(bob);
        assertEquals(2, manager.numSessions());
    }

    /** Test for the {@link SessionManager#write(String, Long, String, String)} method
     * Exception case with invalid session provided.
     */
    @Test
    void testInvalidWriteSession() {
        manager.newSession(alice);
        String bobSession = "1";

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            manager.write(bobSession, (long) 1, title, "New Title");
        });
        assertEquals("Session 1 is not found.", e.getMessage());
    }

    /** Test for the {@link SessionManager#write(String, Long, String, String)} method
     * Exception case with invalid book provided.
     */
    @Test
    void testInvalidWriteBook() {
        String aliceSession = manager.newSession(alice);

        Exception e = assertThrows(BookException.class, () -> {
            manager.write(aliceSession, (long) 3, title, "New Title");
        });
        assertEquals("Not found book with id: 3", e.getMessage());
    }

    /** Test for the {@link SessionManager#write(String, Long, String, String)} method
     * Exception case with invalid field provided.
     */
    @Test
    void testInvalidWriteField() {
        String aliceSession = manager.newSession(alice);

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            manager.write(aliceSession, (long) 1, "Editor", "New Editor");
        });
        assertEquals("Editor is not a valid Book field.", e.getMessage());
    }

    /** Test for the {@link SessionManager#read(String, Long, String)}  method
     * Exception case with invalid session provided.
     */
    @Test
    void testInvalidReadSession() {
        manager.newSession(alice);
        String bobSession = "1";

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            manager.read(bobSession, (long) 1, title);
        });
        assertEquals("Session 1 is not found.", e.getMessage());
    }

    /** Test for the {@link SessionManager#read(String, Long, String)}  method
     * Exception case with invalid book provided.
     */
    @Test
    void testInvalidReadBook() {
        String aliceSession = manager.newSession(alice);

        Exception e = assertThrows(BookException.class, () -> {
            manager.read(aliceSession, (long) 3, title);
        });
        assertEquals("Not found book with id: 3", e.getMessage());
    }

    /** Test for the {@link SessionManager#read(String, Long, String)}  method
     * Exception case with invalid field provided.
     */
    @Test
    void testInvalidReadField() {
        String aliceSession = manager.newSession(alice);

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            manager.read(aliceSession, (long) 1, "Editor");
        });
        assertEquals("Editor is not a valid Book field.", e.getMessage());
    }

    /** Test for the concurrent execution of {@link SessionManager#write(String, Long, String, String)} methods
     * Both success and lock acquisition exception cases.
     */
    @Test
    void testConcurrentWriteSessions() {

        AtomicReference<String> failure = new AtomicReference<>();

        String aliceSession = manager.newSession(alice);
        String bobSession = manager.newSession(bob);

        Thread aliceWrite = new Thread(() -> {
            assertDoesNotThrow(() ->
                    manager.write(aliceSession, (long) 1, title, "New Title Alice")
            );
        });
        aliceWrite.setUncaughtExceptionHandler((th, ex) -> failure.set(ex.getMessage()));
        aliceWrite.start();

        Thread bobWrite = new Thread(() -> {
            Exception e = assertThrows(LockException.class, () -> {
                        Thread.sleep(1000);
                        manager.write(bobSession, (long) 1, title, "New Title Bob");
                    }
            );
            assertEquals("Another user has lock on book 1", e.getMessage());
        });
        bobWrite.setUncaughtExceptionHandler((th, ex) -> failure.set(ex.getMessage()));
        bobWrite.start();

        try {
            aliceWrite.join();
            bobWrite.join();
        } catch (InterruptedException e1) {
            fail("Thread interrupted");
        }

        if (failure.get() != null){
            fail(failure.toString());
        }
    }

    /** Test for the concurrent execution of {@link SessionManager#write(String, Long, String, String)} and
     * {@link SessionManager#read(String, Long, String)} methods
     * Both success and lock acquisition exception cases.
     */
    @Test
    void testConcurrentReadAfterWrite() {

        AtomicReference<String> failure = new AtomicReference<>();

        String aliceSession = manager.newSession(alice);
        String bobSession = manager.newSession(bob);

        Thread aliceWrite = new Thread(() -> {
            assertDoesNotThrow(() ->
                manager.write(aliceSession, (long) 1, title, "New Title Alice")
            );
        });
        aliceWrite.setUncaughtExceptionHandler((th, ex) -> failure.set(ex.getMessage()));
        aliceWrite.start();

        Thread bobRead1 = new Thread(() -> {
            Exception e = assertThrows(LockException.class, () -> {
                        Thread.sleep(1000);
                        manager.read(bobSession, (long) 1, title);
                    }
            );
            assertEquals("Another user has lock on book 1", e.getMessage());
        });
        bobRead1.setUncaughtExceptionHandler((th, ex) -> failure.set(ex.getMessage()));
        bobRead1.start();

        Thread bobRead2 = new Thread(() -> {
            assertDoesNotThrow(() -> {
                        Thread.sleep(2000);
                        assertEquals("Book Two", manager.read(bobSession, (long) 2, title));
                    }
            );
        });
        bobRead2.setUncaughtExceptionHandler((th, ex) -> failure.set(ex.getMessage()));
        bobRead2.start();

        try {
            aliceWrite.join();
            bobRead1.join();
            bobRead2.join();
        } catch (InterruptedException e1) {
            fail("Thread interrupted");
        }

        if (failure.get() != null){
            fail(failure.toString());
        }

    }

}

