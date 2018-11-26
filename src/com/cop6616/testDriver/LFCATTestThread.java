package com.cop6616.testDriver;

import com.cop6616.lfcat.LFCAT;
import com.cop6616.lfcat.Util;

import java.util.*;

public class LFCATTestThread implements Runnable
{
    private LFCAT<Integer> lfcat;
    private int threadops = 0;
    private int heartbeat;
    private Random random;
    private final int maxRange = 1000000;
    private ArrayDeque<Integer> actions;
    private ArrayDeque<Integer> toLookup;
    private ArrayDeque<Integer> toInsert;
    private ArrayDeque<Integer> toRemove;
    private ArrayDeque<Util.Pair<Integer,Integer>> toQuery;
    public static boolean start = false;



    LFCATTestThread(int _threadops, LFCAT<Integer> _lfcat, int range, Ratio targetRatios)
    {
        threadops = _threadops;
        heartbeat = threadops/4;
        lfcat = _lfcat;
        Ratio currentRatios = new Ratio();
        random = new Random();
        actions = new ArrayDeque<Integer>();
        toLookup = new ArrayDeque<Integer>();
        toInsert = new ArrayDeque<Integer>();
        toRemove = new ArrayDeque<Integer>();
        toQuery = new ArrayDeque<Util.Pair<Integer,Integer>>();

        ArrayList<Integer> possibleActions = new ArrayList<>();
        possibleActions.add(0);
        possibleActions.add(1);
        possibleActions.add(2);
        possibleActions.add(3);

        for(int i =0; i < threadops; i ++)
        {
            int rnd = possibleActions.get(random.nextInt(possibleActions.size()));


            if(rnd == 0)
            {
                actions.add(0);

                toLookup.push(random.nextInt(maxRange));

                currentRatios.UpdateContainsCount(1);
            }
            else
            if(rnd == 1)
            {
                actions.add(1);

                toInsert.push(random.nextInt(maxRange));

                currentRatios.UpdateInsertCount(1);
            }
            else
            if(rnd == 2)
            {
                actions.add(2);

                toRemove.push(random.nextInt(maxRange));

                currentRatios.UpdateRemoveCount(1);

            }
            else
            if(rnd == 3)
            {
                actions.add(3);

                Integer lowIndex = random.nextInt(maxRange / 2);
                Integer highIndex = lowIndex + random.nextInt( range);

                toQuery.push(new Util.Pair<>(lowIndex,highIndex));

                currentRatios.UpdateRangeCount(1);
            }

            possibleActions.clear();

            if(currentRatios.containsOperations <= targetRatios.containsOperations)
            {
                possibleActions.add(0);
            }

            if(currentRatios.insertOperations <= targetRatios.insertOperations)
            {
                possibleActions.add(1);
            }

            if(currentRatios.removeOperations <= targetRatios.removeOperations)
            {
                possibleActions.add(2);
            }

            if(currentRatios.rangeOperations <= targetRatios.rangeOperations)
            {
                possibleActions.add(3);
            }
        }

    }

    @Override
    public void run()
    {
        while(!start)
        {
            // Wait for all threads to be created before starting.
        }

        for( int i = 0 ; i < threadops; i ++)
        {
            int action = actions.pop();

            if(action == 0)
            {
                Integer val = toLookup.pop();
                lfcat.Lookup(val);
            }
            else
            if(action == 1)
            {
                Integer val = toInsert.pop();
                lfcat.Insert(val);
            }
            else
            if(action == 2)
            {
                Integer val = toRemove.pop();
                lfcat.Remove(val);
            }
            else
            if(action == 3)
            {
                Util.Pair<Integer,Integer> pair = toQuery.pop();
                lfcat.RangeQuery(pair.a, pair.b);
            }

        }
    }
}
