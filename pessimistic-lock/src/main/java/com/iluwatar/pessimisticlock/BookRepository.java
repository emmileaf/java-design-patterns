/*
 * The MIT License
 * Copyright © 2014-2021 Ilkka Seppälä
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.iluwatar.pessimisticlock;

import java.util.HashMap;
import java.util.Map;

/**
 * Class and all methods modified from package com.iluwatar.versionnumber
 * CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1307
 * This repository represents simplified database.
 * As a typical database do, repository operates with copies of object.
 * So client and repo has different copies of book, which can lead to concurrency conflicts
 * as much as in real databases.
 */
public class BookRepository {
    private final Map<Long, Book> collection = new HashMap<>();

    /**
     * Adds book to collection.
     * Actually we are putting copy of book (saving a book by value, not by reference);
     */
    public void add(Book book) throws BookException {
        if (collection.containsKey(book.getId())) {
            throw new BookException("Duplicated book with id: " + book.getId());
        }

        // add copy of the book
        collection.put(book.getId(), new Book(book));
    }

    /**
     * Updates book in collection
     */
    public void update(Book book) throws BookException {
        if (!collection.containsKey(book.getId())) {
            throw new BookException("Not found book with id: " + book.getId());
        }

        // save book copy to repository
        collection.put(book.getId(), new Book(book));
    }

    /**
     * Returns book representation to the client.
     * Representation means we are returning copy of the book.
     */
    public Book get(long bookId) throws BookException {
        if (!collection.containsKey(bookId)) {
            throw new BookException("Not found book with id: " + bookId);
        }

        // return copy of the book
        return new Book(collection.get(bookId));
    }
}