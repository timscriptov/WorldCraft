package com.mcal.droid.rugl.gl;


public abstract class Facet<T> implements Comparable<T> {
    public abstract void transitionFrom(T t);
}
