package com.cop6616.lfcat;

import com.jwetherell.algorithms.data_structures.AVLTree;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;


public class LFCAT <T extends Comparable<T>>
{
    enum Operation { INSERT, REMOVE}

    AtomicReference<Node> root;

    private static final int MAX_BACKOFF_MILLI = 10;
    private static final int MAX_BACKOFF = 5;

    public LFCAT()
    {
        root = new AtomicReference<Node>();
        Node node = (Node) new BaseNode<T>();
        root.set(node);
    }

    public static void SetTreeSize(int treeSize)
    {
        BaseNode.treeSize = treeSize;
    }

    public boolean Insert(T x)
    {
        return DoUpdate(Operation.INSERT, x);
    }

    public boolean Remove(T x)
    {
        return DoUpdate(Operation.REMOVE, x);
    }

    public boolean Lookup(T x)
    {
        BaseNode<T> base = (BaseNode<T>) Util.FindBaseNode(root.get(), (Integer)x);
        boolean found = base.DataContains(x);

        return found;
    }

    public AVLTree<T> RangeQuery(int lowKey, int highKey)
    {
        return RangeNode.AllInRange(root, lowKey, highKey, null);
    }

    private boolean DoUpdate(Operation op, T x)
    {
        ContentionInfo cnt_info = ContentionInfo.UNCONTESTED;
        int key = (Integer) x;
        Node tryBase = null;
        Random random = new Random();

        int missedContention = 0;

        while(true)
        {
            BaseNode<T> base = (BaseNode<T>)Util.FindBaseNode(root.get(), (Integer)key);

            if(base.IsReplaceable())
            {
                boolean res = false;

                BaseNode<T> newb = new BaseNode<T>(base);

                if (op == Operation.INSERT)
                {
                    res = newb.DataInsert(x);

                }

                if (op == Operation.REMOVE)
                {
                    res = newb.DataRemove(x);
                }

                newb.statistic = base.NewStat(cnt_info);

                tryBase = Util.TryReplace(root, base, newb);
                if(newb == tryBase)
                {
                    newb.AdaptIfNeeded(root);
                    return res;
                }
            }

            missedContention ++;
            cnt_info = ContentionInfo.CONTESTED;

            if(tryBase != null && tryBase.type != NodeType.BASE && tryBase.type != NodeType.ROUTE)
            {
                tryBase.HelpIfNeeded(root);
            }
            else
            {
                try
                {
                    int  clamp = missedContention > MAX_BACKOFF ? MAX_BACKOFF : missedContention;
                    Thread.sleep(random.nextInt(MAX_BACKOFF_MILLI) +
                            (int)Math.pow(2, clamp));

                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

/*
    public boolean HelpIfNeeded(Node n)
    {
        Node temp = n;
        if(temp.type == NodeType.JOIN_NEIGHBOR)
        {
            temp = ((JoinNeighborNode) temp).mainNode;
        }
        if(temp.type == NodeType.JOIN_MAIN)
        {
            if (((JoinMainNode) temp).neighbor2.get() == FlagNode.PREPARING)
            {
                ((JoinMainNode) temp).neighbor2.compareAndSet(FlagNode.PREPARING,FlagNode.ABORTED);
            }
            else if(((JoinMainNode) temp).neighbor2.get() == FlagNode.NEEDS_HELP)
            {
                //TODO: complete merge
            }
        }
        else if(temp.type == NodeType.RANGE ) //TODO: && ((RangeNode) temp).storage.result.get() == Node.NOT_SET)
        {
            //TODO: allInRange
        }

        return true; // default value
    }
*/

    public void Print()
    {
        root.get().Print();
    }

    //Non-Concurrent
    public int Size()
    {
        return root.get().Size();
    }
}