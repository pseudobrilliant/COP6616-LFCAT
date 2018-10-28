package com.cop6616.lfcat.tests;

import com.cop6616.lfcat.LFCAT;
import org.junit.jupiter.api.Test;

import java.util.Vector;

public class LFCATTest
{
    private static final int NUM_THREADS = 5;

    @Test
    void ConcurLFCATInsertTest() throws Exception
    {

        LFCAT<Integer> lfcat = new LFCAT<Integer>();

        Vector<Thread> threads = new Vector<Thread>();

        int range = 100;

        for(int i=0; i < NUM_THREADS; i++)
        {
            LFCATInsertTest r = new LFCATInsertTest(i * range, range, lfcat);

            Thread t = new Thread(r);

            t.start();

            threads.add(t);
        }

        for(int i=0; i < NUM_THREADS; i++)
        {
            threads.elementAt(i).join();
        }
    }


    public class LFCATInsertTest implements Runnable
    {
        LFCAT<Integer> lfcat;
        int start = 0;
        int range =0;

        LFCATInsertTest(int _start, int _range, LFCAT<Integer> _lfcat)
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
