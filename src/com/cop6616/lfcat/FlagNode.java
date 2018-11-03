package com.cop6616.lfcat;

public class FlagNode extends Node
{
    // Static Node Flags for the status of the operations.
    // Implemented as (some_node == Node.FLAG_NODE), if this does not work
    // then implement as (some_node.status == NodeStatus.STATUS)
    //NOT_FOUND is a flag used for treap results in the range so not included here.
    public static final Node PREPARING = new FlagNode(NodeStatus.PREPARING);
    public static final Node ABORTED = new FlagNode(NodeStatus.ABORTED);
    public static final Node NEEDS_HELP = new FlagNode(NodeStatus.NEEDS_HELP);
    public static final Node DONE = new FlagNode(NodeStatus.DONE); // May not be needed

    public FlagNode(NodeStatus _status)
    {
        type = NodeType.FLAG;
        status = _status;
    }

    public boolean IsReplaceable ()
    {
        return true;
    }

    public void Print()
    {

    }

    public int Size()
    {
        return 0;
    }
}
