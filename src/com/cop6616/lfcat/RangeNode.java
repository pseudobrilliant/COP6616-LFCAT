package com.cop6616.lfcat;

import java.util.concurrent.atomic.AtomicReference;

public class RangeNode<T extends Comparable<T>> extends Node
{
    int lokey;
    int hikey;
    //TODO: Result Storage

    private static final int RANGE_CONTRIB = 100;

    public RangeNode()
    {
        type = NodeType.RANGE;
        status = NodeStatus.NONE;
    }

    public RangeNode(int lokey, int hikey)
    {
        type = NodeType.RANGE;
        status = NodeStatus.NONE;
    }

    public boolean IsReplaceable()
    {
        //TODO: Rnage Replace
        return false;
    }

    // Calculates and sets the new contention statistic for a node.
    public int NewStat(ContentionInfo cnt)
    {
        int stat = statistic;

        if(cnt == ContentionInfo.CONTESTED && statistic <= Node.HIGH_CONT)
        {
            stat = statistic + HIGH_CONT_CONTRB - RANGE_CONTRIB;
        }
        else
        if(cnt == ContentionInfo.UNCONTESTED && statistic >= LOW_CONT)
        {
            stat = statistic - LOW_CONT_CONTRB - RANGE_CONTRIB;
        }

        return stat;
    }

    public static boolean AllInRange(AtomicReference<Node> root, int low, int key)
    {
        /*Given a range to find, the Range query operation traverses the structure while saving the traversal in stacks
        and marking relevant nodes as part of the Range Query. The operation begins by traversing the LFCAT structure to
        find the Base node containing the low key in the range. The low Base node, and the parent used to reach it in the
        stack, are marked as part of the Range Query by CAS replacing their nodes with Range node copies. This Base node
        is tracked in a stack of visited nodes that are know to be part of the range. Starting from this Base node this
        operation is repeated on the subsequent furthest left nodes that have yet to be visited. When a Base node that
        contains or exceeds the high range key is reached then we have completed our full list of required base nodes,
        and marked them and their parent as part of the range query. The stack of verified nodes is then traversed in
        stack order in order to join each treap into one result set. */

        return false;
    }
}
