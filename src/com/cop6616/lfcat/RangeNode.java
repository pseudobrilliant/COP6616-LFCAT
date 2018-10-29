package com.cop6616.lfcat;

import com.jwetherell.algorithms.data_structures.AVLTree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RangeNode<T extends Comparable<T>> extends BaseNode<T>
{
    public class ResultStorage<U extends Comparable<U>>
    {
        AtomicReference<AVLTree<U>>  result;
        AtomicBoolean moreThanOneBase;

        public ResultStorage()
        {
            moreThanOneBase.set(false);
        }
    }

    int lokey;
    int hikey;
    ResultStorage<T> storage;

    private static final int RANGE_CONTRIB = 100;

    public RangeNode()
    {
        type = NodeType.RANGE;
        status = NodeStatus.NONE;
    }

    public RangeNode(Node _node, int _lokey, int _hikey, ResultStorage<T> _str)
    {
        type = NodeType.RANGE;
        status = NodeStatus.NONE;

        parent = _node.parent;
        statistic = _node.statistic;

        if(_node.type == NodeType.RANGE || _node.type == NodeType.NORMAL)
        {
            BaseNode<T> bn = (BaseNode<T>) _node;
            data = bn.data;
        }
        else
        {
            data =new AVLTree<T>();
        }

        lokey = _lokey;
        hikey = _hikey;

        storage = _str;
    }

    public boolean IsReplaceable()
    {
        //TODO: Rnage Replace
        return false;
    }

    // Calculates and sets the new contention statistic for a node.
    public int NewStat(ContentionInfo cnt)
    {
        int stat = statistic;

        if(cnt == ContentionInfo.CONTESTED && statistic <= Node.HIGH_CONT)
        {
            stat = statistic + HIGH_CONT_CONTRB - RANGE_CONTRIB;
        }
        else
        if(cnt == ContentionInfo.UNCONTESTED && statistic >= LOW_CONT)
        {
            stat = statistic - LOW_CONT_CONTRB - RANGE_CONTRIB;
        }

        return stat;
    }

    public static void CopyStateTo(Deque<Node> orig, Deque<Node> target)
    {
        target.clear();

        for (Node n : orig)
        {
            target.push(n);
        }
    }

    public static void ReplaceTop(Deque<Node> s, Node n)
    {
        s.pop();
        s.push(n);
    }

    public AVLTree<T> AllInRange(AtomicReference<Node> m, int lowKey, int highKey, ResultStorage<T> rs) //TODO: Add Result Set
    {
        Deque<Node> done = new ArrayDeque<Node>();
        Deque<Node> s = new ArrayDeque<Node>();
        Deque<Node> backup_s = new ArrayDeque<Node>();

        return LowInRange(m, lowKey,highKey,s,backup_s,done,rs);

    }

    public AVLTree<T> LowInRange(AtomicReference<Node> m, int lowKey, int highKey,
                                 Deque<Node> s, Deque<Node> backup_s, Deque<Node> done, ResultStorage<T> rs)
    {
        Node b = FindBaseStack(m.get(), lowKey, s);
        ResultStorage<T> mys = null;
        if (rs != null)
        {
            if (b.type != NodeType.RANGE)
            {
                return rs.result.get();
            }
            else
            {
                RangeNode<T> brn = (RangeNode<T>) b;
                if (brn.storage != rs)
                {
                    return rs.result.get();
                }
                else
                {
                    mys = rs;
                }
            }
        }
        else if (!b.IsReplaceable())
        {
            mys = new ResultStorage();
            RangeNode<T> n = new RangeNode<T>(b, lowKey, highKey, mys);

            if (!TryReplace(m, b, n))
            {
                //I believe this should be rs, as mys has no values yet, and we've failed the swap
                return LowInRange(m, lowKey, highKey, s, backup_s, done, rs);
            }

            ReplaceTop(s, n);
        }
        else if (b.type == NodeType.RANGE && ((RangeNode<T>) b).hikey >= hikey)
        {
            RangeNode<T> brn = (RangeNode<T>) b;
            return AllInRange(m, brn.lokey, brn.hikey, brn.storage);
        }
        else
        {
            //Help_If_Needed
            return LowInRange(m, lowKey, highKey, s, backup_s, done, rs);
        }

        return RestInRange(m, lowKey, highKey, b, s, backup_s, done, mys);
    }

    public AVLTree<T> RestInRange(AtomicReference<Node> m, int lowKey, int highKey, Node b,
                                  Deque<Node> s, Deque<Node> backup_s, Deque<Node> done,
                                  ResultStorage<T> mys)
    {
        while(true)
        {
            done.push(b);
            CopyStateTo(s,backup_s);

            RangeNode<T> brn = (RangeNode<T>) b;

            if(brn.data.isEmpty() && (Integer)brn.data.max() >= highKey)
            {
                break;
            }

            b = FindNextBaseStack(s);

            if(b == null)
            {
                break;
            }
            else if(mys.result.get() != null)
            {
                return (AVLTree<T>)mys.result.get();
            }
            else if(b.type == NodeType.RANGE && ((RangeNode)b).storage ==mys)
            {
                continue;
            }
            else if(b.IsReplaceable())
            {
                RangeNode<T> nr = new RangeNode<>(b,lowKey,highKey,mys);
                if(TryReplace(m,b,nr))
                {
                    ReplaceTop(s,nr);
                }
                else
                {

                }
            }
        }
    }

    public Node FindBaseStack(Node n, int key, Deque<Node> s)
    {
        Node temp = n;

        while(temp.type == NodeType.ROUTE)
        {
            s.push(n);

            if(key < ((RouteNode) temp).key)
            {
                temp = ((RouteNode) temp).left.get();
            }
            else
            {
                temp = ((RouteNode) temp).right.get();
            }
        }

        s.push(n);

        return n;
    }

    public Node FindNextBaseStack(Deque<Node> s)
    {
        RouteNode b = (RouteNode)s.pop();
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

    public Node LeftmostStack(Node b, Deque<Node> s)
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
