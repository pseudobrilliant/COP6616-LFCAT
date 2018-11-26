package com.cop6616.lfcat;

import com.jwetherell.algorithms.data_structures.AVLTree;
import java.util.concurrent.atomic.AtomicReference;

public class JoinNeighborNode<T extends Comparable<T>> extends BaseNode<T>
{
    Node mainNode;


    public JoinNeighborNode(Node _mainNode)
    {
        type = NodeType.JOIN_NEIGHBOR;
        mainNode = _mainNode;
    }

    public JoinNeighborNode(Node _mainNode, Node _neighborNode)
    {
//        JoinNeighborNode(_main);
        type = NodeType.JOIN_NEIGHBOR;
        mainNode = _mainNode;
        parent = _neighborNode.parent;
        data = new AVLTree<T>(((BaseNode<T>)_neighborNode).data); // Use copy constructor
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

    @Override
    public void HelpIfNeeded(AtomicReference<Node> root)
    {
        this.mainNode.HelpIfNeeded(root);
        return;
    }


}
