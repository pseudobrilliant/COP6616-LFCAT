package com.cop6616.lfcat;

import java.util.concurrent.atomic.AtomicReference;


public class LFCAT <T extends Comparable<T>>
{
    enum Operation { INSERT, REMOVE}

    AtomicReference<Node> root;

    public LFCAT()
    {
        root.set(new BaseNode<T>());
    }


    public boolean Insert(int key, T x)
    {
        return DoUpdate(Operation.INSERT, x);
    }

    public boolean Remove(int key)
    {
        return DoUpdate(Operation.REMOVE, key, null);
    }

    public boolean Lookup(int key, T x)
    {
        BaseNode<T> base = (BaseNode<T>) Node.FindBaseNode(root.get(), key);
        return base.Contains(x);
    }

    private boolean DoUpdate(Operation op, int key, T x)
    {
        ContentionInfo cnt_info = ContentionInfo.UNCONTENED;

        while(true)
        {
            Node base = Node.FindBaseNode(root.get(), key);

            if(Node.IsReplaceable(base))
            {
                boolean res = false;

                BaseNode<T> newb = new BaseNode<T>();
                newb.parent = base.parent;

                if(op == Operation.INSERT)
                {
                    res = newb.DataInsert(x);
                }

                if(op == Operation.REMOVE)
                {
                    res = newb.DataRemove(x);
                }

                newb.statistic = Node.NewStat(base, cnt_info);

                if(Node.TryReplace(root, base, newb))
                {
                    Node.AdaptIfNeeded(root, newb);
                    return res;
                }
            }

            cnt_info = ContentionInfo.CONTENDED;
            this.HelpIfNeeded(base)
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
            if (((JoinMainNode) temp).neighbor2.get() == Node.PREPARING)
            {
                ((JoinMainNode) temp).neighbor2.compareAndSet(Node.PREPARING,Node.ABORTED);
            }
            else if(((JoinMainNode) temp).neighbor2.get() == Node.NEEDS_HELP)
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
}