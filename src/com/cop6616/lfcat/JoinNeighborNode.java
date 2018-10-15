package com.cop6616.lfcat;

public class JoinNeighborNode extends Node
{
    Node mainNode;

    public JoinNeighborNode(Node _mainNode)
    {
        type = NodeType.JOIN_NEIGHBOR;
        status = NodeStatus.NONE;
        mainNode = _mainNode;
    }

    // Checks if a node is replacable.
    public boolean IsReplaceable ()
    {
        JoinMainNode main = (JoinMainNode) mainNode; // get main node from neighbor
        if (main.neighbor2.get() == FlagNode.ABORTED || main.neighbor2.get() == FlagNode.DONE)
        {
            return true;
        }

        return false;
    }
}
