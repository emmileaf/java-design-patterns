package com.iluwatar.pessimisticlock;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link SessionManager}
 */
class SessionManagerTest {

    private final BookRepository bookRepo = new BookRepository();
    private SessionManager manager;

    @BeforeEach
    void setUp() throws BookException {
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
    void testAddRemoveSessions() {
        String aliceSession = manager.newSession("Alice");
        assertEquals("0", aliceSession);

        String bobSession = manager.newSession("Bob");
        assertEquals("1", bobSession);

        assertEquals(2, manager.numSessions());
        manager.removeSession("1");
        assertEquals(1, manager.numSessions());
    }

}

