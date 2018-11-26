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
        neighbor1 = null;
        neighbor2 = new AtomicReference<Node>();
        neighbor2.set(FlagNode.PREPARING);
        gparent = null;
        otherb = null;
    }

    public JoinMainNode(Node _mainNode)
    {
        type = NodeType.JOIN_MAIN;
        neighbor1 = null;
        neighbor2 = new AtomicReference<Node>();
        neighbor2.set(FlagNode.PREPARING);
        parent = _mainNode.parent;
        gparent = null;
        otherb = null;
        data = new AVLTree<T>(((BaseNode<T>)_mainNode).data); // Use copy constructor
    }

    // Checks if a node is replacable.
    public boolean IsReplaceable ()
    {
        return (neighbor2.get() == FlagNode.ABORTED);  // Check status of neighbor2
    }

    @Override
    public void HelpIfNeeded(AtomicReference<Node> root)
    {
        if(this.neighbor2.get() == FlagNode.PREPARING)
        {
            this.neighbor2.compareAndSet(FlagNode.PREPARING , FlagNode.ABORTED);
        }
        else if( this.neighbor2.get().type != NodeType.FLAG )
        {
            CompleteJoin(root, this);
        }
    }

    // Sets neighbor2 to ABORTED. Used when a secure join operation fails.
    public static <T extends Comparable<T>> Node Fail0(Node main)
    {
        ((JoinMainNode<T>) main).neighbor2.set(FlagNode.ABORTED);
        return null;
    }

    // Resets the joinID of the parent and calls Fail0. Used when a secure join operation fails.
    public static Node Fail1(Node main)
    {
        ((RouteNode) main.parent).joinID.set(null);
        return Fail0(main);
    }

    // Call this method when a left child needs to be merged with a neighboring node
    public static <T extends Comparable<T>> Node SecureJoinLeft(AtomicReference<Node> root, Node base)
    {

        Node n0 = Util.FindLeftmostBaseNode( ((RouteNode) base.parent).right.get() );
        if( !n0.IsReplaceable() )
            return null;
        
        JoinMainNode<T> main = new JoinMainNode<T>(base); // assign fields from base

        if( !((RouteNode) base.parent).left.compareAndSet( base, main) )
            return null; // Failed to replace BaseNode with JoinMain node

        JoinNeighborNode<T> n1 = new JoinNeighborNode<T>(main, n0);

        if( n1 != Util.TryReplace(root, n0, n1) )
        {
            return Fail0(main);
        }
        if( !((RouteNode) main.parent).joinID.compareAndSet(null, main) )
        {
            return Fail0(main);
        }

        Node gparent = Util.FindParentOf(root, (RouteNode) main.parent);
        if(gparent == FlagNode.NOT_FOUND || (gparent != null && !((RouteNode) gparent).joinID.compareAndSet(null,main)) )
        {
            return Fail1(main);
        }

        main.gparent = gparent;
        main.otherb = ((RouteNode) main.parent).right.get();
        main.neighbor1 = n1;
        
        RouteNode joinedp = (RouteNode) (main.otherb==n1 ? gparent: n1.parent);


        JoinNeighborNode<T> temp = new JoinNeighborNode<T>(main, n1); // assign fields from n1
        temp.parent = joinedp;

        if( main.neighbor2.compareAndSet( FlagNode.PREPARING , temp) )
        {
            return main;
        }

        if(gparent == null)
        {
            return Fail1(main);
        }

        ((RouteNode) gparent).joinID.set(null);

        return null;
    }

    // Call this method when a right child needs to be merged with a neighboring node
    public static <T extends Comparable<T>> Node SecureJoinRight(AtomicReference<Node> root, Node base)
    {
        Node n0 = Util.FindRightmostBaseNode( ((RouteNode) base.parent).right.get() );
        if( !n0.IsReplaceable() )
            return null;

        JoinMainNode<T> main = new JoinMainNode<T>(base); // assign fields from base

        if( !((RouteNode) base.parent).right.compareAndSet( base, main) )
            return null; // Failed to replace BaseNode with JoinMain node

        JoinNeighborNode<T> n1 = new JoinNeighborNode<T>(main, n0);

        if( n1 != Util.TryReplace(root, n0, n1) )
        {
            return Fail0(main);
        }
        if( !((RouteNode) main.parent).joinID.compareAndSet(null, main) )
        {
            return Fail0(main);
        }

        Node gparent = Util.FindParentOf(root, (RouteNode) main.parent);
        if(gparent == FlagNode.NOT_FOUND || (gparent != null && !((RouteNode) gparent).joinID.compareAndSet(null,main)) )
        {
            return Fail1(main);
        }

        main.gparent = gparent;
        main.otherb = ((RouteNode) main.parent).left.get();
        main.neighbor1 = n1;

        RouteNode joinedp = (RouteNode) (main.otherb==n1 ? gparent: n1.parent);


        JoinNeighborNode<T> temp = new JoinNeighborNode<T>(main, n1); // assign fields from n1
        temp.parent = joinedp;

        if( main.neighbor2.compareAndSet( FlagNode.PREPARING , temp) )
        {
            return main;
        }

        if(gparent == null)
        {
            return Fail1(main);
        }

        ((RouteNode) gparent).joinID.set(null);

        return null;
    }

/*
    // Call this method to finish a join (use for both left and right children)
    // This version of CompleteJoin recasts main as a JoinMainNode FOR EACH invocation
    public  static <T extends Comparable<T>> void CompleteJoin(AtomicReference<Node> root, Node main)
    {
        Node n2 = (Node)((JoinMainNode<T>) main).neighbor2.get();
        if(n2 == FlagNode.DONE)
        {
            return;
        }

        Util.TryReplace(root, ((JoinMainNode<T>) main).neighbor1, n2);
        ((RouteNode) main.parent).valid.set(false);

        Node replacement;
        if ( ((JoinMainNode<T>) main).otherb == ((JoinMainNode<T>) main).neighbor1 )
        {
            replacement = n2;
        }
        else
        {
            replacement = ((JoinMainNode<T>) main).otherb;
        }

        if ( ((JoinMainNode<T>) main).gparent == null)
        {
            root.compareAndSet(main.parent, replacement);
        }
        else if( ((RouteNode) ((JoinMainNode<T>) main).gparent).left.get() == main.parent)
        {
            ((RouteNode) ((JoinMainNode<T>) main).gparent).left.compareAndSet(main.parent, replacement);
            ((RouteNode) ((JoinMainNode<T>) main).gparent).joinID.compareAndSet(main, null);
        }
        else if( ((RouteNode) ((JoinMainNode) main).gparent).right.get() == main.parent)
        {
            ((RouteNode) ((JoinMainNode<T>) main).gparent).right.compareAndSet(main.parent, replacement);
            ((RouteNode) ((JoinMainNode<T>) main).gparent).joinID.compareAndSet(main, null);
        }

        ((JoinMainNode) main).neighbor2.set(FlagNode.DONE);
    }

*/

    // Call this method to finish a join (use for both left and right children)
    // This version of CompleteJoin recasts main as a JoinMainNode<T> once at the top
    public static <T extends Comparable<T>> void CompleteJoin(AtomicReference<Node> root, Node main)
    {
        JoinMainNode m = (JoinMainNode<T>) main;
        Node n2 = (Node) m.neighbor2.get();
        if(n2 == FlagNode.DONE)
        {
            return;
        }

        Util.TryReplace(root, m.neighbor1, n2);
        ((RouteNode) m.parent).valid.set(false);

        Node replacement;
        if ( m.otherb == m.neighbor1 )
        {
            replacement = n2;
        }
        else
        {
            replacement = m.otherb;
        }

        if ( m.gparent == null)
        {
            root.compareAndSet(m.parent, replacement);
        }
        else if( ((RouteNode) m.gparent).left.get() == m.parent)
        {
            ((RouteNode) m.gparent).left.compareAndSet(m.parent, replacement);
            ((RouteNode) m.gparent).joinID.compareAndSet(m, null);
        }
        else if( ((RouteNode) m.gparent).right.get() == m.parent)
        {
            ((RouteNode) m.gparent).right.compareAndSet(m.parent, replacement);
            ((RouteNode) m.gparent).joinID.compareAndSet(m, null);
        }

        m.neighbor2.set(FlagNode.DONE);
    }

}
