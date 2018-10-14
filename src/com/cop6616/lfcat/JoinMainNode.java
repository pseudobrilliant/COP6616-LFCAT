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
        super(NodeType.JOIN_MAIN);
        neighbor2.set(Node.PREPARING);
    }
}
