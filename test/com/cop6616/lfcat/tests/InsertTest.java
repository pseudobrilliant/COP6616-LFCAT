package com.cop6616.lfcat.tests;

import com.cop6616.lfcat.LFCAT;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Vector;

public class InsertTest
{
    private static final int NUM_THREADS = 10;

    LFCAT<Integer> lfcat = new LFCAT<Integer>();

    @Test
    void ConcurInsertTest() throws Exception
    {

        Vector<Thread> threads = new Vector<Thread>();

        int range = 100000;

        for(int i=0; i < NUM_THREADS; i++)
        {
            InsertTestThread r = new InsertTestThread(i * range, range, lfcat);

            Thread t = new Thread(r);

            t.start();

            threads.add(t);
        }

        for(int i=0; i < NUM_THREADS; i++)
        {
            threads.elementAt(i).join();
        }

        lfcat.Print();

        assertEquals(range * NUM_THREADS, lfcat.Size());
    }


    public class InsertTestThread implements Runnable
    {
        LFCAT<Integer> lfcat;
        int start = 0;
        int range =0;

        InsertTestThread(int _start, int _range, LFCAT<Integer> _lfcat)
        {
            start = _start;
            range = _range;
            lfcat = _lfcat;
        }

        @Override
        public void run()
        {
            for(int i = start; i < range + start; i ++)
            {
                lfcat.Insert(i);
            }
        }
    }
}
