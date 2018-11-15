package com.cop6616.lfcat;

import com.jwetherell.algorithms.data_structures.AVLTree;

import java.util.concurrent.atomic.AtomicReference;


public class LFCAT <T extends Comparable<T>>
{
    enum Operation { INSERT, REMOVE}

    AtomicReference<Node> root;

    public LFCAT()
    {
        root = new AtomicReference<Node>();
        Node node = (Node) new BaseNode<T>();
        root.set(node);
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
        return base.DataContains(x);
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

        while(true)
        {
            BaseNode<T> base = (BaseNode<T>)Util.FindBaseNode(root.get(), (Integer)key);

            if(base.IsReplaceable())
            {
                boolean res = false;

                BaseNode<T> newb = new BaseNode<T>(base.data);
                newb.parent = base.parent;

                if(op == Operation.INSERT)
                {
                    res = newb.DataInsert(x);
                }

                if(op == Operation.REMOVE)
                {
                    res = newb.DataRemove(x);
                }

                newb.statistic = base.NewStat(cnt_info);

                tryBase = Util.TryReplace(root, base, newb);
                if(base == tryBase)
                {
                    newb.AdaptIfNeeded(root);
                    return res;
                }
            }

            cnt_info = ContentionInfo.CONTESTED;

            if(tryBase != null)
            {
                tryBase.HelpIfNeeded(root);
            }
        }
    }

    //
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
        else if(temp.type == NodeType.RANGE /*TODO: && ((RangeNode) temp).storage.result.get() == Node.NOT_SET*/)
        {
            //TODO: allInRange
        }

        return true; // default value
    }

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