package com.iluwatar.pessimisticlock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests for {@link BookRepository}
 * Modified from package com.iluwatar.versionnumber
 * CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1307
 */
public class BookRepositoryTest {
    private BookRepository repo;
    private Book bookOne = new Book();
    private Book bookTwo = new Book();
    private Book bookOneReplace = new Book();

    @BeforeEach
    void setUp() {
        repo = new BookRepository();
        bookOne.setId((long) 1);
        bookTwo.setId((long) 2);
        bookOneReplace.setId((long) 1);
        bookOne.setTitle("Book One");
        bookTwo.setTitle("Book Two");
        bookOneReplace.setTitle("New Book One");
    }

    /** Test for the {@link BookRepository#add(Book)} method */
    @Test
    void addBookTest() {
        assertDoesNotThrow(() -> repo.add(bookOne));
        Exception e = assertThrows(BookException.class, () -> repo.add(bookOne));
        assertEquals("Duplicated book with id: 1", e.getMessage());
    }

    /** Test for the {@link BookRepository#get(long)} method */
    @Test
    void getBookTest() {
        assertDoesNotThrow(() -> {
                repo.add(bookOne);
                assertEquals(bookOne.getTitle(), repo.get(1).getTitle());
            }
        );
        Exception e = assertThrows(BookException.class, () -> repo.get(2));
        assertEquals("Not found book with id: 2", e.getMessage());
    }

    /** Test for the {@link BookRepository#update(Book)} method */
    @Test
    void updateBookTest() {
        assertDoesNotThrow(() -> {
            repo.add(bookOne);
            repo.update(bookOneReplace);
            assertEquals(bookOneReplace.getTitle(), repo.get(1).getTitle());
        });
        Exception e = assertThrows(BookException.class, () -> repo.update(bookTwo));
        assertEquals("Not found book with id: 2", e.getMessage());
    }
}
