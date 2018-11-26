package com.cop6616.lfcat.tests;

import com.cop6616.lfcat.LFCAT;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Random;
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

        //lfcat.Print();
    }


    public class InsertTestThread implements Runnable
    {
        LFCAT<Integer> lfcat;
        int range =0;
        Random random;
        Vector<Integer> inserted;

        InsertTestThread(int _start, int _range, LFCAT<Integer> _lfcat)
        {
            range = _range;
            lfcat = _lfcat;
            random = new Random();
            inserted = new Vector<Integer>();

            for(int i = 0; i < range; i ++)
            {
                Integer rnd = random.nextInt(1000000);
                inserted.add(rnd);
            }
        }

        @Override
        public void run()
        {
            for(int i = 0; i < range; i ++)
            {
                lfcat.Insert(inserted.elementAt(i));
            }

            for(int i = 0; i < range; i ++)
            {
                if(!lfcat.Lookup(inserted.elementAt(i)))
                {
                    fail();
                }
            }
        }
    }
}
