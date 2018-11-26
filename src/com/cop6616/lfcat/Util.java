package com.cop6616.lfcat;

import java.util.Deque;
import java.util.concurrent.atomic.AtomicReference;

public class Util
{
    public static class Pair<T, U>
    {
        public final T a;
        public final U b;

        public Pair(T _a, U _b)
        {
            a = _a;
            b = _b;
        }
    }

    // Attempts to replace a node.
    public static Node TryReplace(AtomicReference<Node> root, Node b, Node newB)
    {
        if(b.parent == null)
        {
            if(root.compareAndSet(b,newB))
            {
                return newB;
            }
            else
            {
                return root.get();
            }
        }
        else
        {
            RouteNode routeParent = (RouteNode) b.parent;

            if(routeParent.left.get()== b)
            {
                if(routeParent.left.compareAndSet(b,newB))
                {
                    return newB;
                }
                else
                {
                    return routeParent.left.get();
                }
            }

            if(routeParent.right.get() == b)
            {
                if(routeParent.right.compareAndSet(b,newB))
                {
                    return newB;
                }
                else
                {
                    return routeParent.right.get();
                }
            }
        }

        return null;
    }

    // Finds and returns the base node for a given key.
    public static Node FindBaseNode(Node n, int key)
    {
        Node temp = n;
        while(temp.type == NodeType.ROUTE)
        {
            if(key <= ((RouteNode) temp).key)
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
            return FlagNode.NOT_FOUND;
        }

        return prevNode;
    }

    public static Node FindBaseStack(Node n, int key, Deque<Node> s)
    {
        Node temp = n;

        s.clear();

        while(temp.type == NodeType.ROUTE)
        {
            s.push(temp);

            if(key <= ((RouteNode) temp).key)
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
        if(s.size() > 0)
        {
            Node b = s.pop();

            if (s.isEmpty())
            {
                return null;
            }

            Node t = s.getFirst();

            if(t.type == NodeType.ROUTE)
            {
                RouteNode r = (RouteNode) t;

                if (r.left.get() == b)
                {
                    return LeftmostStack(r.right.get(), s);
                }

                int target = r.key;

                while (r != null)
                {
                    if (r.valid.get() && r.key > target)
                    {
                        return LeftmostStack(r.right.get(), s);
                    }
                    else
                    {
                        s.pop();
                        if (s.isEmpty())
                        {
                            return null;
                        }
                        else
                        {
                            t = s.getFirst();
                            if (t.type != NodeType.ROUTE)
                            {
                                return null;
                            }
                            else
                            {
                                r = (RouteNode) t;
                            }
                        }
                    }
                }
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

        s.push(n);

        return n;
    }

}
