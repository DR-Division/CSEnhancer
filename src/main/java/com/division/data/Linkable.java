package com.division.data;

public interface Linkable<T> {

    T getPrevious();

    T getNext();

    void setPrevious(T item);

    void setNext(T item);
}
