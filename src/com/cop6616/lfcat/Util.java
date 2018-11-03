package com.cop6616.lfcat;

import java.util.Deque;
import java.util.concurrent.atomic.AtomicReference;

public class Util
{

    // Attempts to replace a node.
    public static boolean TryReplace(AtomicReference<Node> root, Node b, Node newB)
    {
        if(b.parent == null)
        {
            return root.compareAndSet(b,newB);
        }
        else
        {
            RouteNode routeParent = (RouteNode) b.parent;

            if(routeParent.left.get()== b)
            {
                return routeParent.left.compareAndSet(b,newB);
            }

            if(routeParent.right.get() == b)
            {
                return routeParent.right.compareAndSet(b,newB);
            }
        }

        return false;
    }

    // Finds and returns the base node for a given key.
    public static Node FindBaseNode(Node n, int key)
    {
        Node temp = n;
        while(temp.type == NodeType.ROUTE)
        {
            if(key < ((RouteNode) temp).key)
            {
                temp = ((RouteNode) temp).left.get();
            }
            else
            {
                temp = ((RouteNode) temp).right.get();
            }
        }

        return temp;
    }

    // Finds and returns the leftmost base node under a given node.
    public static Node FindLeftmostBaseNode(Node n)
    {
        Node temp = n;
        while(temp.type == NodeType.ROUTE)
        {
            temp = ((RouteNode) temp).left.get();
        }

        return temp;
    }

    // Finds and returns the rightmost base node under a given node.
    public static Node FindRightmostBaseNode(Node n)
    {
        Node temp = n;
        while(temp.type == NodeType.ROUTE)
        {
            temp = ((RouteNode) temp).right.get();
        }

        return temp;
    }

    // Finds and returns the parent of a node.
    public static Node FindParentOf(AtomicReference<Node> root, RouteNode n)
    {
        Node prevNode = null;
        Node currentNode = root.get();
        while(currentNode != n && currentNode.type == NodeType.ROUTE)
        {
            prevNode = currentNode;
            if(n.key <= ((RouteNode)currentNode).key)
            {
                currentNode = ((RouteNode) currentNode).left.get();
            }
            else
            {
                currentNode = ((RouteNode) currentNode).right.get();
            }
        }
        if(currentNode.type != NodeType.ROUTE)
        {
            return null;
        }

        return prevNode;
    }

    public static Node FindBaseStack(Node n, int key, Deque<Node> s)
    {
        Node temp = n;

        while(temp.type == NodeType.ROUTE)
        {
            s.push(temp);

            if(key < ((RouteNode) temp).key)
            {
                temp = ((RouteNode) temp).left.get();
            }
            else
            {
                temp = ((RouteNode) temp).right.get();
            }
        }

        s.push(temp);

        return temp;
    }

    public static Node FindNextBaseStack(Deque<Node> s)
    {
        Node b = s.pop();
        RouteNode t = (RouteNode)s.getFirst();

        if(t == null)
        {
            return null;
        }

        if(t.type == NodeType.ROUTE && t.left.get() == b)
        {
            return LeftmostStack(t.right.get(), s);
        }

        int target = t.key;

        while(t != null)
        {
            if(t.valid.get() && t.key > target)
            {
                return LeftmostStack(t.right.get(), s);
            }
            else
            {
                s.pop();
                t = (RouteNode)s.getFirst();
            }
        }

        return null;
    }

    public static Node LeftmostStack(Node b, Deque<Node> s)
    {
        Node n = b;
        while(n.type == NodeType.ROUTE)
        {
            s.push(n);
            n = ((RouteNode)n).left.get();
        }

        return n;
    }
}
