package com.iluwatar.pessimisticlock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link Book}
 * Modified from package com.iluwatar.versionnumber
 * CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1307
 */
public class BookTest {
    private Book orig = new Book();

    @BeforeEach
    void setUp() {
        orig.setId((long) 1);
        orig.setTitle("Book Title");
        orig.setTitle("Book Author");
    }

    /** Test for the Book copy constructor */
        @Test
        void copyBookTest() {
            Book copy = new Book(orig);
            assertEquals(orig.getId(), copy.getId());
            assertEquals(orig.getTitle(), copy.getTitle());
            assertEquals(orig.getAuthor(), copy.getAuthor());
    }

    /** Test for the {@link Book#getTitle()} and {@link Book#setTitle(String)} methods */
    @Test
    void getSetTitleTest() {
        orig.setTitle("New Title");
        assertEquals("New Title", orig.getTitle());
    }

    /** Test for the {@link Book#getAuthor()} and {@link Book#setAuthor(String)} methods */
    @Test
    void getSetAuthorTest() {
        orig.setAuthor("New Author");
        assertEquals("New Author", orig.getAuthor());
    }

    /** Test for the {@link Book#getId()} and {@link Book#setId(Long)} methods */
    @Test
    void getSetIdTest() {
        orig.setId((long) 5);
        assertEquals((long) 5, orig.getId());
    }

}