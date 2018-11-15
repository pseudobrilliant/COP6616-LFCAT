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

    public JoinMainNode(Node _mainNode)
    {
        type = NodeType.JOIN_MAIN;
        status = NodeStatus.NONE;
        neighbor1 = null;
        neighbor2.set(FlagNode.PREPARING);
        gparent = null;
        otherb = null;
        data = new AVLTree<T>(((BaseNode<T>)_mainNode).data); // Use copy constructor
    }

    // Checks if a node is replacable.
    public boolean IsReplaceable ()
    {
        return (neighbor2.get() == FlagNode.ABORTED);  // Check status of neighbor2
    }

    public static Node SecureJoinLeft(AtomicReference<Node> root, Node base)
    {/*
        Node n0 = Util.FindLeftmostBaseNode( ((RouteNode) base.parent).right.get() );
        if( !n0.IsReplaceable() )
            return null;
        
        Node main = new JoinMainNode(base); // assign fields from base

        if(! ((RouteNode) base.parent).left.compareAndExchange( base, main) )
            return null; // Failed to replace BaseNode with JoinMain node

        Node n1 = new JoinNeighborNode(main, n0)
        
        if( !Util.TryReplace(root, n0, n1) )
            goto fail0;
        if( !compareAndExchange( ((RouteNode) m.parent).join_id, null, main) )
            goto fail0;

/*Keep going from here*/
    /*
        Node grandparent = parent_of(t, m->parent);
        if(gparent == NOT_FOUND || (gparent != NULL && !CAS(&gparent ->join_id ,NULL,m)))
            goto fail1;

        m->gparent = gparent;
        m->otherb = aload(&m->parent ->right);
        m->neigh1 = n1;
        
        node* joinedp = m->otherb==n1 ? gparent: n1->parent;
        if(CAS(&m->neigh2 , PREPARING , new node{... = n1, // assign fields from n1
                                            type = join_neighbor ,
                                            parent = joinedp ,
                                            main_node = m,
                                            data = treap_join(m, n1)}))
            return m;

        if(gparent == NULL) goto fail1;
        astore(&gparent ->join_id , NULL);

fail1: astore(&m->parent ->join_id , NULL);
fail0: astore(&m->neigh2 , ABORTED);*/
        return null;
}

    public static Node SecureJoinRight(AtomicReference<Node> root, Node base)
    {
        return null;
    }

    // This version of CompleteJoin recasts main as a JoinMainNode FOR EACH invocation
    public static void CompleteJoin(AtomicReference<Node> root, Node main)
    {
        Node n2 = (Node)((JoinMainNode) main).neighbor2.get();
        if(n2 == FlagNode.DONE)
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


/*
    // This version of CompleteJoin recasts main as a JoinMainNode once at the top
    public static void CompleteJoin(AtomicReference<Node> root, Node main)
    {
        JoinMainNode m = (JoinMainNode) main;
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
            root.compareAndExchange(m.parent, replacement);
        }
        else if( ((RouteNode) m.gparent).left.get() == m.parent)
        {
            ((RouteNode) m.gparent).left.compareAndExchange(m.parent, replacement);
            ((RouteNode) m.gparent).joinID.compareAndExchange(m, null);
        }
        else if( ((RouteNode) m.gparent).right.get() == m.parent)
        {
            ((RouteNode) m.gparent).right.compareAndExchange(m.parent, replacement);
            ((RouteNode) m.gparent).joinID.compareAndExchange(m, null);
        }

        m.neighbor2.set(FlagNode.DONE);
    }
*/

}
