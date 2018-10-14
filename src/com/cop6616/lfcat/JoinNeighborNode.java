package com.cop6616.lfcat;

public class JoinNeighborNode extends Node
{
    Node mainNode;

    public JoinNeighborNode(Node _mainNode)
    {
        super(NodeType.JOIN_NEIGHBOR, NodeStatus.NONE);
        mainNode = _mainNode;
    }
}
