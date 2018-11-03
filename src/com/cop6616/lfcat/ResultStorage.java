package com.cop6616.lfcat;

import com.jwetherell.algorithms.data_structures.AVLTree;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ResultStorage<U extends Comparable<U>>
{
    AtomicReference<AVLTree<U>> result;
    AtomicBoolean moreThanOneBase;

    public ResultStorage()
    {
        moreThanOneBase.set(false);
    }
}
