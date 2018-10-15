package com.cop6616.lfcat;

import java.util.concurrent.atomic.AtomicReference;

enum ContentionInfo
{
    CONTESTED,
    UNCONTESTED,
    NOINFO
}

enum NodeType
{
    ROUTE,
    FLAG,
    NORMAL,
    JOIN_MAIN,
    JOIN_NEIGHBOR,
    RANGE
}

// Used in conjunction with the Static Node Flags
enum NodeStatus
{
    NONE,
    PREPARING,
    ABORTED,
    NEEDS_HELP,
    DONE
}

public abstract class Node
{
    NodeType type;
    NodeStatus status;
    Node parent;

    int statistic = 0;

    static final int HIGH_CONT = 1000;
    static final int LOW_CONT = -1000;
    static final int HIGH_CONT_CONTRB = 250;
    static final int LOW_CONT_CONTRB = 1;

    public abstract boolean IsReplaceable();

    public int NewStat(ContentionInfo cnt)
    {
        int stat = statistic;

        if(cnt == ContentionInfo.CONTESTED && statistic <= Node.HIGH_CONT)
        {
            stat = statistic + HIGH_CONT_CONTRB;
        }
        else
        if(cnt == ContentionInfo.UNCONTESTED && statistic >= LOW_CONT)
        {
            stat = statistic - LOW_CONT_CONTRB;
        }

        return stat;
    }

    // Attempts to replace a node.
    public static boolean TryReplace(AtomicReference<Node> root, Node b, Node newB)
    {
        if(b.parent == null)
        {
            return root.compareAndSet(b,newB);
        }
        else
        {
            RouteNode routeParent = (RouteNode) b.parent;

            if(routeParent.left.get()== b)
            {
                return routeParent.left.compareAndSet(b,newB);
            }

            if(routeParent.right.get() == b)
            {
                return routeParent.right.compareAndSet(b,newB);
            }
        }

        return false;
    }

    // Finds and returns the base node for a given key.
    public static Node FindBaseNode(Node n, int key)
    {
        Node temp = n;
        while(temp.type == NodeType.ROUTE)
        {
            if(key < ((RouteNode) temp).key)
            {
                temp = ((RouteNode) temp).left.get();
            }
            else
            {
                temp = ((RouteNode) temp).right.get();
            }
        }

        return temp;
    }

}
