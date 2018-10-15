package com.cop6616.lfcat;

import java.util.concurrent.atomic.AtomicReference;

public class JoinMainNode extends Node
{
    Node neighbor1;
    AtomicReference<Node> neighbor2;
    Node gparent;
    Node otherb;

    public JoinMainNode()
    {
        type = NodeType.JOIN_MAIN;
        status = NodeStatus.NONE;
        neighbor2.set(FlagNode.PREPARING);
    }

    // Checks if a node is replacable.
    public boolean IsReplaceable ()
    {
        return (neighbor2.get() == FlagNode.ABORTED);  // Check status of neighbor2
    }

}
