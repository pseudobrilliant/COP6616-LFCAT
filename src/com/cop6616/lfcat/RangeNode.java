package com.cop6616.lfcat;

import com.jwetherell.algorithms.data_structures.AVLTree;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Random;
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
    }

    /***
     * Range node constructor that takes in a node with data, a rage and a result storage container
     * @param _node Node to copy base values from to populate the range node
     * @param _lokey low value of the range
     * @param _hikey high value of the range
     * @param _str storage container for the result
     */
    public RangeNode(Node _node, int _lokey, int _hikey, ResultStorage<T> _str)
    {
        type = NodeType.RANGE;

        //Copy basic parent and statistic values
        parent = _node.parent;
        statistic = _node.statistic;

        if (_node.type == NodeType.RANGE || _node.type == NodeType.BASE ||
                _node.type == NodeType.JOIN_MAIN || _node.type == NodeType.JOIN_NEIGHBOR)
        {
            //Copy the given nodes data if available
            BaseNode<T> bn = (BaseNode<T>) _node;
            data = new AVLTree<>(bn.data);
        }

        lokey = _lokey;
        hikey = _hikey;

        storage = _str;
    }

    /***
     * The result storage of a range node is only set when the full operation is completed and all range nodes are joined
     * @return
     */
    public boolean IsReplaceable()
    {
        return storage.result.get() != null;
    }


    /***
     * Other operations fail the CAS and want to help can attempt to help complete the ongoing range operation
     * @param root
     */
    public void HelpIfNeeded(AtomicReference<Node> root)
    {
        if (this.storage.result.get() == null)
        {
            AllInRange(root, lokey, hikey, storage);
        }
    }

    @Override
    /***
     * New stat update that takes into account the range contribution to help push low contention adaptation
     */
    public int NewStat(ContentionInfo cnt)
    {
        int stat = statistic;

        if (cnt == ContentionInfo.CONTESTED && statistic <= Node.HIGH_CONT)
        {
            stat = statistic + HIGH_CONT_CONTRB - RANGE_CONTRIB;
        }
        else if (cnt == ContentionInfo.UNCONTESTED && statistic >= LOW_CONT)
        {
            stat = statistic - LOW_CONT_CONTRB - RANGE_CONTRIB;
        }

        if(data.size() >= BaseNode.treeSize)
        {
            stat += MAX_SIZE_CNTB;
        }

        return stat;
    }

    /***
     * Stack operation to backup stack
     * @param orig
     * @param target
     */
    public static void CopyStateTo(Deque<Node> orig, Deque<Node> target)
    {
        target.clear();

        Object[] arr = orig.toArray();

        for (int i = orig.size()-1; i >= 0; i --)
        {
            target.push((Node)arr[i]);
        }
    }

    /***
     * Replaces the top of the stack. Primarily used to replace a node with the resulting range node.
     * @param s
     * @param n
     */
    public static void ReplaceTop(Deque<Node> s, Node n)
    {
        s.pop();
        s.push(n);
    }

    /***
     * Starts the range search by creating stack values to be kept track of throughout the operation.
     * @param root root of search
     * @param lowKey low key in range
     * @param highKey high key in range
     * @param rs result storage passed in as part of a continuing range operation
     * @param <T>
     * @return
     */
    public static <T extends Comparable<T>> AVLTree<T> AllInRange(AtomicReference<Node> root, int lowKey, int highKey, ResultStorage<T> rs)
    {
        Deque<Node> s = new ArrayDeque<Node>();
        Deque<Node> backup_s = new ArrayDeque<Node>();
        Deque<Node> done = new ArrayDeque<Node>();

        return LowInRange(root, lowKey, highKey, s, backup_s, done, rs); // Equates to find_first on line 168 of paper
    }

    /***
     * Searches for the lowest node in the range, replaces it with a range node copy, and kicks of the search for the
     * continuing range.
     * @param root root of search
     * @param lowKey low key in range
     * @param highKey high key in range
     * @param s stack for current traversal
     * @param backup_s back up of current traversal in case operation currently fails
     * @param done keeps track of finished nodes.
     * @param rs result storage passed in as part of a continuing range operation
     * @param <T>
     * @return
     */
    public static <T extends Comparable<T>> AVLTree<T> LowInRange(AtomicReference<Node> root, int lowKey, int highKey,
                                                                  Deque<Node> s, Deque<Node> backup_s, Deque<Node> done, ResultStorage<T> rs)
    {
        Node b;
        ResultStorage<T> mys = null;

        while (true)
        {
            //Finds the node matching to the lowest key in the search range.
            b = Util.FindBaseStack(root.get(), lowKey, s);

            //If result storage passed in is not null then we are to looking to assist another range operation
            if (rs != null)
            {
                //If the base node found is either not a range node, or matches the result storage
                //Then the current state does not match with what we can help with and so the operation returns.
                if (b.type != NodeType.RANGE || ((RangeNode<T>) b).storage != rs)
                {
                    return rs.result.get();
                }
                else
                {
                    //If the base node is a range node and matches then we store rs in myrs to work on it
                    //in the next phase.
                    mys = rs;
                }
            }
            //If the rs is null then we are starting a range fresh. Meaning we must find the first base node
            //and mark it as the start of the range operation, before moving onto more nodes in the range.
            else if (b.IsReplaceable())
            {
                //Create the storage node, populate it with a copy of the base node and the range data
                //Including the mys result storage which will be shared by all the range node operations.
                mys = new ResultStorage<T>();
                RangeNode<T> n = new RangeNode<T>(b, lowKey, highKey, mys);

                //Tries to replace the base node with the new range node.
                b = Util.TryReplace(root, b, n);

                if (n != b)
                {
                    continue;
                }

                //If the replace works we need to make sure our current stack represents this as well.
                ReplaceTop(s, n);
            }
            //If another range node is found with a higher range we attempt to help
            else if (b.type == NodeType.RANGE && ((RangeNode<T>) b).hikey >= highKey)
            {
                RangeNode<T> brn = (RangeNode<T>) b;
                return AllInRange(root, brn.lokey, brn.hikey, brn.storage);
            }
            else
            {
                //b may be another type of operation or an overlaping range (but lower height), try to help
                b.HelpIfNeeded(root);
                continue;
            }

            return RestInRange(root, lowKey, highKey, b, s, backup_s, done, mys); // RestInRange is the while loop on line 184 in paper
        }
    }

    /***
     * Continues the search from the stack found in the low key search
     * @param root root of search
     * @param lowKey low key in range
     * @param highKey high key in range
     * @param s stack for current traversal
     * @param backup_s back up of current traversal in case operation currently fails
     * @param done keeps track of finished nodes.
     * @param rs result storage passed in as part of a continuing range operation
     * @param <T>
     * @return
     */
    public static <T extends Comparable<T>> AVLTree<T> RestInRange(AtomicReference<Node> root, int lowKey, int highKey, Node b,
                                                                   Deque<Node> s, Deque<Node> backup_s, Deque<Node> done,
                                                                   ResultStorage<T> rs)
    {
        AVLTree<T> avl = null;

        while (true)
        {
            done.push(b);
            CopyStateTo(s, backup_s);

            BaseNode<T> brn = (BaseNode<T>) b;

            if (!brn.data.isEmpty() && (Integer) brn.data.max() >= highKey)
            {
                break;
            }

            Util.Pair<Node, AVLTree<T>> pair = RestInRangeSearch(root, lowKey, highKey, s, backup_s, done, rs); // RestInRangeSearch equates to find_next_base_node on line 188 of paper

            b = pair.a;
            avl = pair.b;

            if (avl != null)
            {
                return avl;
            }

            if (b == null)
            {
                break;
            }
        }

        return CompleteRangeSearch(root, done, rs); // Equates to 208-214 in paper
    }

    public static <T extends Comparable<T>> Util.Pair<Node, AVLTree<T>> RestInRangeSearch(AtomicReference<Node> root, int lowKey, int highKey,
                                                                                     Deque<Node> s, Deque<Node> backup_s, Deque<Node> done,
                                                                                     ResultStorage<T> rs)
    {

        while(true)
        {
            Node b = Util.FindNextBaseStack(s);

            if (b != null)
            {

                if (rs.result.get() != null)
                {
                    return new Util.Pair<Node, AVLTree<T>>(b, rs.result.get());
                }
                else if (b.type == NodeType.RANGE && ((RangeNode) b).storage == rs)
                {
                    return new Util.Pair<Node, AVLTree<T>>(b, null);
                }
                else if (b.IsReplaceable())
                {
                    RangeNode<T> nr = new RangeNode<>(b, lowKey, highKey, rs);
                    if (nr == Util.TryReplace(root, b, nr))
                    {
                        ReplaceTop(s, nr);
                        return new Util.Pair<Node, AVLTree<T>>(b, null);
                    }
                    else
                    {
                        CopyStateTo(backup_s, s);
                        continue;
                    }
                }
                else
                {
                    b.HelpIfNeeded(root);
                    CopyStateTo(backup_s, s);
                    continue;
                }
            }

            return new Util.Pair<Node, AVLTree<T>>(b, CompleteRangeSearch(root, done, rs));  // CompleteRangeSearch equates to 208-214 in paper
        }
    }

    public static <T extends Comparable<T>> AVLTree<T> CompleteRangeSearch(AtomicReference<Node> root, Deque<Node> done,
                                                                           ResultStorage<T> rs)
    {
        Random rand = new Random();

        Object[] arr = done.toArray();

        AVLTree<T> avl = null;

        for (Object r : arr)
        {
            BaseNode<T> rn = (BaseNode<T>) r;

            if (avl == null)
            {
                avl = new AVLTree<T>(rn.data);
            }
            else
            {
                avl.join(rn.data);
            }
        }

        if (rs.result.compareAndSet(null, avl) && done.size() > 1)
        {
            rs.moreThanOneBase.set(true);
        }

        int rindex = rand.nextInt(done.size());

        BaseNode<T> rbn = (BaseNode<T>) arr[rindex];

        rbn.AdaptIfNeeded(root);

        return rs.result.get();
    }

}
