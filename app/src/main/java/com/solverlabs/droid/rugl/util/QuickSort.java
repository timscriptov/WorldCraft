package com.solverlabs.droid.rugl.util;

import androidx.annotation.NonNull;

import java.util.Comparator;
import java.util.Random;

public class QuickSort {
    private static final Random rng = new Random();

    private QuickSort() {
    }

    public static <T> void sort(T[] array, Comparator<T> comp) {
        sort(array, comp, 0, array.length - 1);
    }

    public static <T> void sort(T[] array, Comparator<T> comp, int lo, int hi) {
        if (hi > lo) {
            int pivotIndex = partition(array, comp, lo, hi, rng.nextInt(hi - lo) + lo);
            sort(array, comp, lo, pivotIndex - 1);
            sort(array, comp, pivotIndex + 1, hi);
        }
    }

    private static <T> int partition(@NonNull T[] array, Comparator<T> comp, int lo, int hi, int pivotIndex) {
        T pivot = array[pivotIndex];
        swap(array, pivotIndex, hi);
        int index = lo;
        for (int i = lo; i < hi; i++) {
            if (comp.compare(array[i], pivot) <= 0) {
                swap(array, i, index);
                index++;
            }
        }
        swap(array, hi, index);
        return index;
    }

    private static <T> void swap(@NonNull T[] array, int i, int j) {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
