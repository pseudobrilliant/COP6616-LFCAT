package com.cop6616.lfcat;

import com.jwetherell.algorithms.data_structures.AVLTree;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/***
 * Class used to store the result of any given range operation
 * @param <T>
 */
public class ResultStorage<T extends Comparable<T>>
{
    AtomicReference<AVLTree<T>> result;
    AtomicBoolean moreThanOneBase;

    public ResultStorage()
    {
        result = new AtomicReference<AVLTree<T>>();
        moreThanOneBase = new AtomicBoolean();

        result.set(null);
        moreThanOneBase.set(false);
    }
}
