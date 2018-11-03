package com.cop6616.lfcat;

import com.jwetherell.algorithms.data_structures.AVLTree;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RangeNode<T extends Comparable<T>> extends BaseNode<T>
{

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
        return storage.result.get() != null;
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

    public static <T extends Comparable<T>> AVLTree<T> AllInRange(AtomicReference<Node> m, int lowKey, int highKey, ResultStorage<T> rs) //TODO: Add Result Set
    {
        Deque<Node> done = new ArrayDeque<Node>();
        Deque<Node> s = new ArrayDeque<Node>();
        Deque<Node> backup_s = new ArrayDeque<Node>();

        return LowInRange(m, lowKey,highKey,s,backup_s,done,rs);
    }

    public static <T extends Comparable<T>> AVLTree<T> LowInRange(AtomicReference<Node> m, int lowKey, int highKey,
                                 Deque<Node> s, Deque<Node> backup_s, Deque<Node> done, ResultStorage<T> rs)
    {
        Node b = Util.FindBaseStack(m.get(), lowKey, s);
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
            mys = new ResultStorage<T>();
            RangeNode<T> n = new RangeNode<T>(b, lowKey, highKey, mys);

            if (!Util.TryReplace(m, b, n))
            {
                //I believe this should be rs, as mys has no values yet, and we've failed the swap
                return LowInRange(m, lowKey, highKey, s, backup_s, done, rs);
            }

            ReplaceTop(s, n);
        }
        else if (b.type == NodeType.RANGE && ((RangeNode<T>) b).hikey >= highKey)
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

    public static <T extends Comparable<T>> AVLTree<T> RestInRange(AtomicReference<Node> m, int lowKey, int highKey, Node b,
                                  Deque<Node> s, Deque<Node> backup_s, Deque<Node> done,
                                  ResultStorage<T> rs)
    {
        AVLTree<T> avl = null;

        while(true)
        {
            done.push(b);
            CopyStateTo(s,backup_s);

            RangeNode<T> brn = (RangeNode<T>) b;

            if(!brn.data.isEmpty() && (Integer)brn.data.max() >= highKey)
            {
                break;
            }

            avl = RestInRangeSearch(m, lowKey,highKey, s, backup_s, done, rs);
        }

        if(avl != null)
        {
            return avl;
        }
        else
        {
            return CompleteRangeSearch(m, done, rs);
        }
    }

    public static <T extends Comparable<T>> AVLTree<T> RestInRangeSearch(AtomicReference<Node> m, int lowKey, int highKey,
                                  Deque<Node> s, Deque<Node> backup_s, Deque<Node> done,
                                  ResultStorage<T> rs)
    {
        Node b = Util.FindNextBaseStack(s);

        if(b != null)
        {
            if (rs.result.get() != null)
            {
                return (AVLTree<T>) rs.result.get();
            }
            else if (b.type == NodeType.RANGE && ((RangeNode) b).storage == rs)
            {
                RestInRangeSearch(m, lowKey, highKey, s, backup_s, done, rs);
                return RestInRangeSearch(m, lowKey, highKey, s, backup_s, done, rs);
            }
            else if (b.IsReplaceable())
            {
                RangeNode<T> nr = new RangeNode<>(b, lowKey, highKey, rs);
                if (Util.TryReplace(m, b, nr))
                {
                    ReplaceTop(s, nr);
                }
                else
                {
                    CopyStateTo(backup_s, s);
                    return RestInRangeSearch(m, lowKey, highKey, s, backup_s, done, rs);
                }
            }
            else
            {
                //TODO: help if needed
                CopyStateTo(backup_s, s);
                return RestInRangeSearch(m, lowKey, highKey, s, backup_s, done, rs);
            }
        }

        return CompleteRangeSearch(m, done, rs);
    }

    public static <T extends Comparable<T>> AVLTree<T> CompleteRangeSearch(AtomicReference<Node> m, Deque<Node> done,
                                        ResultStorage<T> rs)
    {
        Random rand = new Random();

        Object[] arr = done.toArray();

        AVLTree<T> avl = null;

        for(Object r: arr)
        {
            BaseNode<T> rn = (BaseNode<T>) r;
            if(avl == null)
            {
                avl = rn.data;
            }
            else
            {
                avl.join(rn.data);
            }
        }

        if(rs.result.compareAndSet(null, avl) && done.size() > 1)
        {
            rs.moreThanOneBase.set(true);
        }

        int rindex = rand.nextInt(done.size());

        BaseNode<T> rbn = (BaseNode<T>)arr[rindex];

        rbn.AdaptIfNeeded(m);

        return rs.result.get();
    }

}
