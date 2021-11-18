package com.iluwatar.pessimisticlock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SessionManager}
 */
class SessionManagerTest {

    private BookRepository bookRepo;
    private SessionManager manager;

    @BeforeEach
    void setUp() throws BookException {
        bookRepo = new BookRepository();
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

    @Test
    void testNewSession() {
        String aliceSession = manager.newSession("Alice");
        assertEquals("0", aliceSession);
        String bobSession = manager.newSession("Bob");
        assertEquals("1", bobSession);
    }

    @Test
    void testRemoveSession() {
        String aliceSession = manager.newSession("Alice");
        String bobSession = manager.newSession("Bob");
        manager.removeSession("1");
        assertEquals(1, manager.numSessions());
    }

    @Test
    void testNumSessions() {
        assertEquals(0, manager.numSessions());
        String aliceSession = manager.newSession("Alice");
        String bobSession = manager.newSession("Bob");
        assertEquals(2, manager.numSessions());
    }

    @Test
    void testInvalidWriteSession() {
        String aliceSession = manager.newSession("Alice");
        String bobSession = "1";

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            manager.write(bobSession, (long) 1, "Title", "New Title");
        });
        assertEquals("Session 1 is not found.", e.getMessage());
    }

    @Test
    void testInvalidWriteBook() {
        String aliceSession = manager.newSession("Alice");

        Exception e = assertThrows(BookException.class, () -> {
            manager.write(aliceSession, (long) 3, "Title", "New Title");
        });
        assertEquals("Not found book with id: 3", e.getMessage());
    }

    @Test
    void testInvalidWriteField() {
        String aliceSession = manager.newSession("Alice");

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            manager.write(aliceSession, (long) 1, "Editor", "New Editor");
        });
        assertEquals("Editor is not a valid Book field.", e.getMessage());
    }

    @Test
    void testInvalidReadSession() {
        String aliceSession = manager.newSession("Alice");
        String bobSession = "1";

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            String result = manager.read(bobSession, (long) 1, "Title");
        });
        assertEquals("Session 1 is not found.", e.getMessage());
    }

    @Test
    void testInvalidReadBook() {
        String aliceSession = manager.newSession("Alice");

        Exception e = assertThrows(BookException.class, () -> {
            String result = manager.read(aliceSession, (long) 3, "Title");
        });
        assertEquals("Not found book with id: 3", e.getMessage());
    }

    @Test
    void testInvalidReadField() {
        String aliceSession = manager.newSession("Alice");

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            String result = manager.read(aliceSession, (long) 1, "Editor");
        });
        assertEquals("Editor is not a valid Book field.", e.getMessage());
    }

    @Test
    void testConcurrentWriteSessions() {

        AtomicReference<String> failure = new AtomicReference<>();

        String aliceSession = manager.newSession("Alice");
        String bobSession = manager.newSession("Bob");

        Thread aliceWrite = new Thread(() -> {
            assertDoesNotThrow(() ->
                    manager.write(aliceSession, (long) 1, "Title", "New Title Alice")
            );
        });
        aliceWrite.setUncaughtExceptionHandler((th, ex) -> failure.set(ex.getMessage()));
        aliceWrite.start();

        Thread bobWrite = new Thread(() -> {
            Exception e = assertThrows(LockException.class, () -> {
                        Thread.sleep(1000);
                        manager.write(bobSession, (long) 1, "Title", "New Title Bob");
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

    @Test
    void testConcurrentReadAfterWrite() {

        AtomicReference<String> failure = new AtomicReference<>();

        String aliceSession = manager.newSession("Alice");
        String bobSession = manager.newSession("Bob");

        Thread aliceWrite = new Thread(() -> {
            assertDoesNotThrow(() ->
                manager.write(aliceSession, (long) 1, "Title", "New Title Alice")
            );
        });
        aliceWrite.setUncaughtExceptionHandler((th, ex) -> failure.set(ex.getMessage()));
        aliceWrite.start();

        Thread bobRead1 = new Thread(() -> {
            Exception e = assertThrows(LockException.class, () -> {
                        Thread.sleep(1000);
                        manager.read(bobSession, (long) 1, "Title");
                    }
            );
            assertEquals("Another user has lock on book 1", e.getMessage());
        });
        bobRead1.setUncaughtExceptionHandler((th, ex) -> failure.set(ex.getMessage()));
        bobRead1.start();

        Thread bobRead2 = new Thread(() -> {
            assertDoesNotThrow(() -> {
                        Thread.sleep(2000);
                        assertEquals("Book Two", manager.read(bobSession, (long) 2, "Title"));
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

