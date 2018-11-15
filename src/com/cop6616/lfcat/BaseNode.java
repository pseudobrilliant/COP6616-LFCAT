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

    public void HelpIfNeeded(AtomicReference<Node> lfcat)
    {
      return;
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
            SplitAdaptation(m, this);
        }
        else if(NewStat(ContentionInfo.NOINFO) < LOW_CONT)
        {
            //TODO: Join Adaptation
            //JoinAdaptation(m);
        }
    }

    // Splits a treap if there is high contention
    public static <T extends Comparable<T>> void SplitAdaptation(AtomicReference<Node> m, BaseNode b)
    {
        if(b.DataCount() >= 2)
        {
            int split_key = (Integer) b.data.getSplitKey();

            AVLTree.Pair<T> pair = b.data.split();

            BaseNode<T> left = new BaseNode<T>(pair.getLesser());

            BaseNode<T> right = new BaseNode<T>(pair.getGreater());

            RouteNode r = new RouteNode(split_key, left, right);

            left.parent = r;

            right.parent = r;

            Util.TryReplace(m, b, r);
        }
    }

    public int Size()
    {
        return data.size();
    }

    // Joins a node with its right- 0r left-hand neighbor if there is low contention
    public static <T extends Comparable<T>> void JoinAdaptation(AtomicReference<Node> root, Node b)
    {
        /*
        if(b.parent == null)
        {
            return;
        }

        if( ((RouteNode) b.parent).left.get() == b )
        {
            Node m = SecureJoinLeft(root, b);
            if (m != NULL)
            {
                completeJoin(root, m);
            }
        }
        else if ( ((RouteNode) b.parent).right.get() == b )
        {
            Node m = SecureJoinRight(root, b);
            if (m != NULL)
            {
                completeJoin(root, m);
            }
        }*/
    }

    public void Print()
    {
        int size = data.size();
        int min = (Integer)data.min();
        Integer max = (Integer)data.max();

        System.out.println("Base Node - Size: " + size +" Min: " + min + " Max: " + max );
    }
}
