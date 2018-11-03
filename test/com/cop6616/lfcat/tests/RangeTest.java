package com.cop6616.lfcat.tests;

import com.cop6616.lfcat.LFCAT;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Vector;

public class RangeTest
{
    private static final int NUM_THREADS = 5;

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
            Assert.fail();
        }
    }

    @Test
    void ConcurRangeTest() throws Exception
    {

        Vector<Thread> threads = new Vector<Thread>();

        int range = 100;

        for(int i=0; i < NUM_THREADS; i++)
        {
            ConcurRangeTestThread r = new ConcurRangeTestThread(i,i+range, lfcat);

            Thread t = new Thread(r);

            t.start();

            threads.add(t);
        }

        for(int i=0; i < NUM_THREADS; i++)
        {
            threads.elementAt(i).join();
        }

        Assert.assertEquals(range * NUM_THREADS, lfcat.Size());
    }


    public class ConcurRangeTestThread implements Runnable
    {
        LFCAT<Integer> lfcat;
        int start = 0;
        int range =0;
        boolean pass = false;

        ConcurRangeTestThread(int _start, int _range, LFCAT<Integer> _lfcat)
        {
            start = _start;
            range = _range;
            lfcat = _lfcat;
        }

        @Override
        public void run()
        {
            lfcat.RangeQuery(start, range);
        }
    }
}
