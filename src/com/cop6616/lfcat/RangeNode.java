package com.cop6616.lfcat;

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
}
