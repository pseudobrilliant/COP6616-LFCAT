package com.cop6616.lfcat;

import java.util.concurrent.atomic.AtomicReference;

enum ContentionInfo
{
    CONTENDED,
    UNCONTENED,
    NOINFO
}

enum NodeType
{
    ROUTE,
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
    NOT_FOUND,
    ABORTED,
    NEEDS_HELP,
    DONE
}

public class Node
{
    NodeType type;
    NodeStatus status;
    Node parent;

    int statistic = 0;

    private static final int HIGH_CONT = 1000;
    private static final int LOW_CONT = -1000;
    private static final int HIGH_CONT_CONTRB = 250;
    private static final int LOW_CONT_CONTRB = 1;
    private static final int RANGE_CONTRIB = 100;

    // Static Node Flags for the status of the operations.
    // Implemented as (some_node == Node.FLAG_NODE), if this does not work
    // then implement as (some_node.status == NodeStatus.STATUS)
    public static final Node PREPARING = new Node(NodeType.NORMAL,NodeStatus.PREPARING);
    public static final Node NOT_FOUND = new Node(NodeType.NORMAL,NodeStatus.NOT_FOUND);
    public static final Node ABORTED = new Node(NodeType.NORMAL,NodeStatus.ABORTED);
    public static final Node NEEDS_HELP = new Node(NodeType.NORMAL,NodeStatus.NEEDS_HELP);
    public static final Node DONE = new Node(NodeType.NORMAL,NodeStatus.DONE); // May not be needed

    // Constructors
    public Node( NodeType _type)
    {
        type = _type;
        status = NodeStatus.NONE;
    }

    public Node( NodeType _type, NodeStatus _status)
    {
        type = _type;
        status = _status;
    }

    // Checks if a node is replacable.
    public static boolean IsReplaceable (Node n)
    {
        if(n.type == NodeType.NORMAL)
        {
            return true;
        }
        else if(n.type == NodeType.JOIN_MAIN)
        {
            if (((JoinMainNode) n).neighbor2.get() == Node.ABORTED)  // Check status of neighbor2
            {
                return true;
            }

/* Original version of preceeding "if"
            Node n2 = ((JoinMainNode) n).neighbor2.get();
            if (n2.status == NodeStatus.ABORTED)
            {
               return true;
            }
*/
        }
        else if(n.type == NodeType.JOIN_NEIGHBOR)
        {
            JoinMainNode main = (JoinMainNode) ((JoinNeighborNode) n).mainNode; // get main node from neighbor
            if (main.neighbor2.get() == Node.ABORTED || main.neighbor2.get() == Node.DONE)
            {
                return true;
            }
        }
        else if(n.type == NodeType.RANGE)
        {
            //TODO: Implement Range
        }

        return false;
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

    // Calculates and sets the new contention statistic for a node.
    public static int NewStat(Node n, ContentionInfo cnt)
    {
        int stat = n.statistic;
        int range_contrib = 0;

        if(n.type == NodeType.RANGE)
        {
            range_contrib = RANGE_CONTRIB;
        }

        if(cnt == ContentionInfo.CONTENDED && n.statistic <= HIGH_CONT)
        {
            stat = n.statistic + HIGH_CONT_CONTRB - range_contrib;
        }
        else
        if(cnt == ContentionInfo.UNCONTENED && n.statistic >= LOW_CONT)
        {
            stat = n.statistic - LOW_CONT_CONTRB - range_contrib;
        }

        return stat;
    }

    // Checks the contention statistic of a node and splits or joins as needed
    public static void AdaptIfNeeded(AtomicReference<Node> m, Node b)
    {
        if(! Node.IsReplaceable(b))
            return;

        if(Node.NewStat(b, ContentionInfo.NOINFO) > HIGH_CONT)
        {
            SplitAdaptation(m, b);
        }
        else if(Node.NewStat(b, ContentionInfo.NOINFO) < LOW_CONT)
        {
            //TODO: Join Adaptation
            //JoinAdaptation(m, b);
        }
    }

    // Splits a treap if there is high contention
    public static void SplitAdaptation(AtomicReference<Node> m, Node b)
    {
        BaseNode base = (BaseNode<>)
        if(b.)
    }

    // Joins a node with its right-hand neighbor if there is low contention
    public static void JoinAdaptation(AtomicReference<Node> m, Node b)
    {
        //TODO: Find left-most neighbor and merge
    }
}
