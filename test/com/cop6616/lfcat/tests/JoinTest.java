package com.cop6616.lfcat.tests;

import com.cop6616.lfcat.LFCAT;
import com.jwetherell.algorithms.data_structures.AVLTree;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Random;
import java.util.Vector;

public class JoinTest
{
    private static final int NUM_THREADS = 5;
    private static final int NUM_SEARCHES = 100;
    private static final int MAX_RANGE = 10000;

    LFCAT<Integer> lfcat;

    @Test
    void ConcurJoinTest() throws Exception
    {
        InsertTest insert = new InsertTest();
        try
        {
            insert.ConcurInsertTest();
            lfcat = insert.lfcat;
        }
        catch (Exception e)
        {
        }

        System.out.println("Insert Complete");
        System.out.flush();

        Vector<Thread> threads = new Vector<Thread>();
        Vector<ConcurJoinTestThread> rangeTests = new Vector<ConcurJoinTestThread>();

        for(int i=0; i < NUM_THREADS; i++)
        {
            ConcurJoinTestThread r = new ConcurJoinTestThread(lfcat);

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

        lfcat.Print();
    }


    public class ConcurJoinTestThread implements Runnable
    {
        LFCAT<Integer> lfcat;
        boolean pass = true;
        AVLTree<Integer> tree;
        Random random;


        ConcurJoinTestThread(LFCAT<Integer> _lfcat)
        {
            lfcat = _lfcat;
            random = new Random();
        }

        @Override
        public void run()
        {
            for(int i=0; i < NUM_SEARCHES; i++)
            {
                int size = lfcat.Size();
                Integer lowIndex = random.nextInt(size / 2);
                Integer highIndex = lowIndex + random.nextInt(MAX_RANGE);

                tree = lfcat.RangeQuery(lowIndex, highIndex);

                if(tree.isEmpty())
                {
                    pass = false;
                }
            }
        }
    }
}
