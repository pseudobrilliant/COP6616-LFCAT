package com.cop6616.lfcat;

import com.jwetherell.algorithms.data_structures.AVLTree;

public class JoinNeighborNode<T extends Comparable<T>> extends BaseNode<T>
{
    Node mainNode;


    public JoinNeighborNode(Node _mainNode)
    {
        type = NodeType.JOIN_NEIGHBOR;
        status = NodeStatus.NONE;
        mainNode = _mainNode;
    }

    public JoinNeighborNode(Node _mainNode, Node _neighborNode)
    {
//        JoinNeighborNode(_main);
        type = NodeType.JOIN_NEIGHBOR;
        status = NodeStatus.NONE;
        mainNode = _mainNode;
        parent = _neighborNode.parent;
        data = new AVLTree<T>(((BaseNode<T>)_neighborNode).data);
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
