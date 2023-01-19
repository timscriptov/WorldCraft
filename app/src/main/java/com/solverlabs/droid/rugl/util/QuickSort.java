package com.solverlabs.droid.rugl.util;

import androidx.annotation.NonNull;

import java.util.Comparator;
import java.util.Random;

/**
 * A garbage-free randomised quicksort
 *
 * @author ryanm
 */
public class QuickSort {
    private static final Random rng = new Random();

    private QuickSort() {
        // no instances
    }

    /**
     * @param <T>
     * @param array
     * @param comp
     */
    public static <T> void sort(T[] array, Comparator<T> comp) {
        sort(array, comp, 0, array.length - 1);
    }

    /**
     * @param <T>
     * @param array
     * @param comp
     * @param lo
     * @param hi
     */
    public static <T> void sort(T[] array, Comparator<T> comp, int lo, int hi) {
        if (hi > lo) {
            // choose random element to pivot on
            int pivotIndex = rng.nextInt(hi - lo) + lo;

            // swap elements till everything smaller than the pivot lies
            // before it
            pivotIndex = partition(array, comp, lo, hi, pivotIndex);

            // recurse
            sort(array, comp, lo, pivotIndex - 1);
            sort(array, comp, pivotIndex + 1, hi);
        }
    }

    /**
     * Shuffle elements around to partially-sorted low/pivot/high order
     *
     * @param <T>
     * @param array
     * @param comp
     * @param lo
     * @param hi
     * @param pivotIndex the starting index of the pivot element
     * @return The new index of the pivot element
     */
    private static <T> int partition(@NonNull T[] array, Comparator<T> comp, int lo, int hi,
                                     int pivotIndex) {
        T pivot = array[pivotIndex];

        // send pivot to the back
        swap(array, pivotIndex, hi);

        // index of low/high boundary
        int index = lo;

        for (int i = lo; i < hi; i++) {
            if (comp.compare(array[i], pivot) <= 0) { // element is lower or equal to the pivot
                // swap it to the low region
                swap(array, i, index);
                index++;
            }
        }

        swap(array, hi, index);

        return index;
    }

    /**
     * Swaps elements
     *
     * @param <T>
     * @param array
     * @param i
     * @param j
     */
    private static <T> void swap(@NonNull T[] array, int i, int j) {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}