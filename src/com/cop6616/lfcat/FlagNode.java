package com.cop6616.lfcat;

import java.util.concurrent.atomic.AtomicReference;

public class FlagNode extends Node
{
    // Static Node Flags for the status of the operations.
    // Implemented as (some_node == Node.FLAG_NODE), if this does not work
    // then implement as (some_node.status == NodeStatus.STATUS)
    //NOT_FOUND is a flag used for treap results in the range so not included here.
    public static final Node PREPARING = new FlagNode();
    public static final Node ABORTED = new FlagNode();
    public static final Node NEEDS_HELP = new FlagNode(); // Remove this when HelpIfNeeded is removed from LFCAT
    public static final Node DONE = new FlagNode();
    public static final Node NOT_FOUND = new FlagNode();

    public FlagNode()
    {
        type = NodeType.FLAG;
    }

    public boolean IsReplaceable ()
    {
        return true;
    }

    public void HelpIfNeeded(AtomicReference<Node> root){return;}

    public void Print()
    {

    }

    public int Size()
    {
        return 0;
    }
}
