---
layout: pattern
title: Pessimistic Lock
folder: pessimistic-lock
permalink: /patterns/pessimistic-lock/
categories:
 - Concurrency
language: en
tags:
 - Enterprise Integration Pattern
 - Data access
---

## Name / classification
Pessimistic Lock Pattern

## Intent
The pessimistic lock pattern is used to prevent conflicts during concurrent transactions by allowing data access for one transaction at a time.

It differs from optimistic locking, such as the use of the version number pattern, in that conflicts are prevented altogether rather than detected at the end of the transactions.

## Explanation

Real world example (similar to Version Number Pattern)

> Alice and Bob are working on books which are stored in a database. They are making changes simultaneously involving computationally extensive operations, and some mechanism is needed to prevent them from overwriting each other or accessing out-of-date information.

In plain words

> Pessimistic lock pattern grants protection against concurrent updates to a resource by allowing one request to access at a time.

**Programmatic Example**

We have a `SessionManager` entity, which keeps track of locks on all objects and manages session access to all resources using exclusive read locks:

```java
public class SessionManager {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final Map<Long, String> locks;
    private final Map<String, Session> sessions;
    private final BookRepository bookRepo;
    private int sessionId;

    public SessionManager(BookRepository repo) {
        this.bookRepo = repo;
        this.sessions = new HashMap<>();
        this.locks = new HashMap<>();
        this.sessionId = 0;
    }

    public String newSession(String user) {...}
    public void removeSession(String sessionId) {...}
    public int numSessions() {...}

    // Executes a write request initiated by a given user session on a book.
    public void write(String sessionId, Long bookId, String writeField, String writeValue)
            throws LockException, BookException, IllegalArgumentException, InterruptedException {...}

    // Executes a read request initiated by a given user session on a book.
    public String read(String sessionId, Long bookId, String readField)
            throws LockException, BookException, IllegalArgumentException {...}

    // Checks if a user has lock obtained for a resource
    private boolean hasLock(Long lockable, String owner) {...}

    // Have a user acquire lock on a resource
    private void acquireLock(Long lockable, String owner) throws BookException, LockException {...}

    // Have a user release lock on a resource
    private void releaseLock(Long lockable, String owner) {...}

    // Have a user release currently held locks on all resources
    private void releaseAll(String owner) {...}
}
```

Here's the concurrency control in action:

```java
// set up a SessionManager to let user sessions access the book repository
SessionManager sessionManager = new SessionManager(bookRepository);

// Alice and Bob represent two concurrent user sessions
String aliceSession = sessionManager.newSession("Alice");
String bobSession = sessionManager.newSession("Bob");
String title = "Title";
long book1Id = 1;

// Alice performs an operation-extensive write operation on a book
new Thread(() -> {
    LOGGER.info("Alice initiated WRITE operation on book {}.", book1Id);
    sessionManager.write(aliceSession, book1Id, title, "Harry Potter");
    LOGGER.info("EXPECTED: Alice performed WRITE operation on book {}.", book1Id);
}).start();

// When Bob performs concurrent read on the same book, a LockException should be thrown
new Thread(() -> {
    try {
        LOGGER.info("Bob initiated READ operation on book {}.", book1Id);
        sessionManager.read(bobSession, book1Id, title);
        LOGGER.error("UNEXPECTED: Bob performed READ on book {}, but shouldn't be allowed.", book1Id);
    } catch (LockException e1) {
        LOGGER.info("EXPECTED: Bob is unable to perform READ on book {} while Alice edits.", book1Id);
    }
}).start();
```

Program output:

```java
Alice initiated WRITE operation on book 1.
Bob initiated READ operation on book 1.
SessionManager: Could not acquire lock on book 1.
EXPECTED: Bob is unable to perform READ on book 1 while Alice edits.
```


## Class diagram

## Applicability

Use pessimistic locking for concurrency control:
- When the chance of conflict is high
- When the cost of conflict is high
- As a complementary method to optimistic locking

## Known uses
- [Hibernate](https://allaroundjava.com/pessimistic-locking-hibernate/)
- [JPA](https://www.objectdb.com/java/jpa/persistence/lock)

## Consequences


## Credits
- [Patterns of Enterprise Application Architecture, p426](https://martinfowler.com/eaaCatalog/pessimisticOfflineLock.html)
- [Offline Concurrency Control](https://www.baeldung.com/cs/offline-concurrency-control)
- [Pessimistic Locking in JPA](https://www.baeldung.com/jpa-pessimistic-locking)
