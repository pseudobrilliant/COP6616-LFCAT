package com.cop6616.lfcat;

import java.util.concurrent.atomic.AtomicReference;

//Possible levels of contention
enum ContentionInfo
{
    CONTESTED,
    UNCONTESTED,
    NOINFO
}

//Types of supported nodes, used for casting
enum NodeType
{
    ROUTE,
    FLAG, // This type is used to distinguish special cases in lieu of abusing pointers.
    BASE, // This is the normal node_type from the paper. We just call them base nodes.
    JOIN_MAIN,
    JOIN_NEIGHBOR,
    RANGE
}

/***
 * Abstract Node class describes the most basic functionality expected of nodes in the LFCAT.
 * All nodes inheirt from this class and are expected to implement (or override) these functions as needed.
 * Each node should be able to state whether it is in a replaceable state, whether it requires assistance in
 * finishing an ongoing operation, and how to update the nodes contention statistic value.
 */
public abstract class Node
{
    //For casting purposes all nodes should be provided a type in the constructor
    NodeType type;

    //All nodes should be able to have a parent as part of the tree structure.
    Node parent;

    //Base and range nodes should provide a statistic on the amount of contentions
    int statistic = 0;

    //Contention adaptation thresholds and deltas for contention occurences.
    static final int HIGH_CONT = 1000;
    static final int LOW_CONT = -1000;
    static final int HIGH_CONT_CONTRB = 250;
    static final int LOW_CONT_CONTRB = 1;

    //Whether the node is in a replaceable state, should be implented by each respective node type.
    public abstract boolean IsReplaceable();

    //Whether the node is in a state that could use help, should be implented by each respective node type.
    public abstract void HelpIfNeeded(AtomicReference<Node> root);

    public abstract void Print();

    public abstract int Size();

    //Updates the current statistic with a basic update based on the current level of contention found by an operation.
    public int NewStat(ContentionInfo cnt)
    {
        int stat = statistic;

        if(cnt == ContentionInfo.CONTESTED && statistic <= Node.HIGH_CONT)
        {
            stat = statistic + HIGH_CONT_CONTRB;
        }
        else
        if(cnt == ContentionInfo.UNCONTESTED && statistic >= Node.LOW_CONT)
        {
            stat = statistic - LOW_CONT_CONTRB;
        }

        return stat;
    }


}
