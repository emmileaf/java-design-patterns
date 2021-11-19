package com.iluwatar.pessimisticlock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link Book}
 */
public class BookTest {
    private Book orig = new Book();

    @BeforeEach
    void setUp() {
        orig.setId((long) 1);
        orig.setTitle("Book Title");
        orig.setTitle("Book Author");
    }

    @Test
    void copyBookTest() {
        Book copy = new Book(orig);
        assertEquals(orig.getId(), copy.getId());
        assertEquals(orig.getTitle(), copy.getTitle());
        assertEquals(orig.getAuthor(), copy.getAuthor());
    }

    @Test
    void getSetTitleTest() {
        orig.setTitle("New Title");
        assertEquals("New Title", orig.getTitle());
    }

    @Test
    void getSetAuthorTest() {
        orig.setAuthor("New Author");
        assertEquals("New Author", orig.getAuthor());
    }

    @Test
    void getSetIdTest() {
        orig.setId((long) 5);
        assertEquals((long) 5, orig.getId());
    }

}
