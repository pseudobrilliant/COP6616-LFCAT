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
    DONE,
    NOT_FOUND
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

    public abstract void HelpIfNeeded(AtomicReference<Node> lfcat);

    public abstract void Print();

    public abstract int Size();

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
