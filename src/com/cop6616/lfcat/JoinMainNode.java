package com.cop6616.lfcat;

import java.util.concurrent.atomic.AtomicReference;
import com.jwetherell.algorithms.data_structures.AVLTree;

public class JoinMainNode<T extends Comparable<T>> extends BaseNode<T>
{
    Node neighbor1;
    AtomicReference<Node> neighbor2;
    Node gparent;
    Node otherb;


    public JoinMainNode()
    {
        type = NodeType.JOIN_MAIN;
        status = NodeStatus.NONE;
        neighbor1 = null;
        neighbor2.set(FlagNode.PREPARING);
        gparent = null;
        otherb = null;
    }

    public JoinMainNode(Node _mainNode, Node _neighborNode)
    {
        type = NodeType.JOIN_MAIN;
        status = NodeStatus.NONE;
        neighbor1 = null;
        neighbor2.set(FlagNode.PREPARING);
        gparent = null;
        otherb = null;
        data = new AVLTree<T>(((BaseNode<T>)_neighborNode).data);
    }

    // Checks if a node is replacable.
    public boolean IsReplaceable ()
    {
        return (neighbor2.get() == FlagNode.ABORTED);  // Check status of neighbor2
    }

    public static Node SecureJoinLeft(AtomicReference<Node> root, Node base)
    {
        return null;
    }
    public static Node SecureJoinRight(AtomicReference<Node> root, Node base)
    {
        return null;
    }

    public static void CompleteJoin(AtomicReference<Node> root, Node main)
    {
        Node n2 = (Node)((JoinMainNode) main).neighbor2.get();
        if(n2.status == NodeStatus.DONE)
        {
            return;
        }

        Util.TryReplace(root, ((JoinMainNode) main).neighbor1, n2);
        ((RouteNode) main.parent).valid.set(false);

        Node replacement;
        if ( ((JoinMainNode) main).otherb == ((JoinMainNode) main).neighbor1 )
        {
            replacement = n2;
        }
        else
        {
            replacement = ((JoinMainNode) main).otherb;
        }

        if ( ((JoinMainNode) main).gparent == null)
        {
            root.compareAndExchange(main.parent, replacement);
        }
        else if( ((RouteNode) ((JoinMainNode) main).gparent).left.get() == main.parent)
        {
            ((RouteNode) ((JoinMainNode) main).gparent).left.compareAndExchange(main.parent, replacement);
            ((RouteNode) ((JoinMainNode) main).gparent).joinID.compareAndExchange(main, null);
        }
        else if( ((RouteNode) ((JoinMainNode) main).gparent).right.get() == main.parent)
        {
            ((RouteNode) ((JoinMainNode) main).gparent).right.compareAndExchange(main.parent, replacement);
            ((RouteNode) ((JoinMainNode) main).gparent).joinID.compareAndExchange(main, null);
        }

        ((JoinMainNode) main).neighbor2.set(FlagNode.DONE);
    }


}
