package com.cop6616.lfcat.tests;

import com.cop6616.lfcat.LFCAT;
import com.jwetherell.algorithms.data_structures.AVLTree;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Vector;

public class RangeTest
{
    private static final int NUM_THREADS = 8;

    LFCAT<Integer> lfcat;

    RangeTest()
    {
        InsertTest insert = new InsertTest();
        try
        {
            insert.ConcurInsertTest();
            lfcat = insert.lfcat;
        }
        catch (Exception e)
        {
            fail();
        }
    }

    @Test
    void ConcurShortRangeTest() throws Exception
    {

        Vector<Thread> threads = new Vector<Thread>();
        Vector<ConcurRangeTestThread> rangeTests = new Vector<ConcurRangeTestThread>();

        int range = 10;

        for(int i=0; i < NUM_THREADS; i++)
        {
            ConcurRangeTestThread r = new ConcurRangeTestThread(i* range,range, lfcat);

            Thread t = new Thread(r);

            t.start();

            threads.add(t);

            rangeTests.add(r);
        }

        for(int i=0; i < NUM_THREADS; i++)
        {
            threads.elementAt(i).join();

            if(!rangeTests.elementAt(i).pass)
            {
                fail();
            }
        }
    }

    @Test
    void ConcurLongRangeTest() throws Exception
    {

        Vector<Thread> threads = new Vector<Thread>();
        Vector<ConcurRangeTestThread> rangeTests = new Vector<ConcurRangeTestThread>();

        int range = 40000;

        for(int i=0; i < NUM_THREADS; i++)
        {
            ConcurRangeTestThread r = new ConcurRangeTestThread(i* range,range, lfcat);

            Thread t = new Thread(r);

            t.start();

            threads.add(t);

            rangeTests.add(r);
        }

        for(int i=0; i < NUM_THREADS; i++)
        {
            threads.elementAt(i).join();

            if(!rangeTests.elementAt(i).pass)
            {
                fail();
            }
        }
    }


    public class ConcurRangeTestThread implements Runnable
    {
        LFCAT<Integer> lfcat;
        int start = 0;
        int range =0;
        boolean pass = true;
        AVLTree<Integer> tree;


        ConcurRangeTestThread(int _start, int _range, LFCAT<Integer> _lfcat)
        {
            start = _start;
            range = _range;
            lfcat = _lfcat;
        }

        @Override
        public void run()
        {
            tree = lfcat.RangeQuery(start, start + range);

            for(int i = start; i < start + range; i++)
            {
                if(tree == null || !tree.contains(i))
                {
                   pass = false;
                }
            }
        }
    }
}
