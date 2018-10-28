package com.cop6616.lfcat;

import com.jwetherell.algorithms.data_structures.AVLTree;

import java.util.concurrent.atomic.AtomicReference;

public class BaseNode<T extends Comparable<T>> extends Node
{
    AVLTree<T> data;

    public BaseNode()
    {
        type = NodeType.NORMAL;
        status = NodeStatus.NONE;
        data = new AVLTree<T>();
    }

    public BaseNode(AVLTree<T> _data)
    {
        type = NodeType.NORMAL;
        data = new AVLTree<T>(_data);
    }

    public boolean DataInsert(T x)
    {
        return data.add(x);
    }

    public boolean DataRemove(T x)
    {
        return data.remove(x) == x;
    }

    public boolean DataContains(T x)
    {
        return data.contains(x);
    }

    public int DataCount()
    {
        return data.size();
    }

    public boolean IsReplaceable ()
    {
            return true;
    }

    // Checks the contention statistic of a node and splits or joins as needed
    public void AdaptIfNeeded(AtomicReference<Node> m)
    {
        if(!IsReplaceable())
        {
            return;
        }

        if(NewStat(ContentionInfo.NOINFO) > HIGH_CONT)
        {
            SplitAdaptation(m);
        }
        else if(NewStat(ContentionInfo.NOINFO) < LOW_CONT)
        {
            //TODO: Join Adaptation
            //JoinAdaptation(m);
        }
    }

    // Splits a treap if there is high contention
    public void SplitAdaptation(AtomicReference<Node> m)
    {
        if(DataCount() >= 2) // Should this be >= 2 instead?
        {
            int split_key = (Integer) data.getSplitKey();

            AVLTree.Pair<T> pair = data.split();

            BaseNode<T> left = new BaseNode<T>(pair.getLesser());

            System.out.println(left.data.toString());

            BaseNode<T> right = new BaseNode<T>(pair.getGreater());

            RouteNode r = new RouteNode(split_key, left, right);

            left.parent = r;

            right.parent = r;

            Node.TryReplace(m, this, r);
        }
    }

    // Joins a node with its right-hand neighbor if there is low contention
    public void JoinAdaptation(AtomicReference<Node> m, Node b)
    {
        //TODO: Find left-most neighbor and merge
    }

}
