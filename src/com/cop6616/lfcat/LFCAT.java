package com.cop6616.lfcat;

import com.jwetherell.algorithms.data_structures.AVLTree;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/***
 * Lock Free Contention Adaptive Tree that allows for both fine and coarse grained operations over the same structure,
 * while adapting with the types of operations being applied over time. Individual operations like the insert remove and
 * contains would benefit from fine grained access, while range query operations that require multiple values benefit from
 * coarse grained locks. To adapt the standard key value structure to best perform over a miz of these operations in a concurrent
 * environment we allow for adaptations to high contention with repeated accesss, and low contention with broad range.
 * In addtion to the usage statistics for adaptation we also limit the size of the nodes to optimize the tree copy / management.
 * This implementation attempts to provide a generic form of the LFCAT structure with insert, rmeove, contains, and
 * range operations.
 * @param <T>
 */
public class LFCAT <T extends Comparable<T>>
{
    enum Operation { INSERT, REMOVE}

    //Atomic reference to the root of the LFCAT
    AtomicReference<Node> root;

    private static final int MAX_BACKOFF_MILLI = 10;
    private static final int MAX_BACKOFF = 5;

    /***
     * Constructor initializes an empty base node
     */
    public LFCAT()
    {
        root = new AtomicReference<Node>();
        Node node = (Node) new BaseNode<T>();
        root.set(node);
    }

    /***
     * Sets the max size of the tree to adapt to
     * @param treeSize
     */
    public static void SetTreeSize(int treeSize)
    {
        BaseNode.treeSize = treeSize;
    }

    /***
     * Inserts a value into the lfcat targeted at the base node with a matching range
     * @param x value to insert
     * @return Returns whether insert was successful
     */
    public boolean Insert(T x)
    {
        return DoUpdate(Operation.INSERT, x);
    }

    /***
     * Removes a value from the lfcat, targeted at the base node with a matching range.
     * @param x value to remove
     * @return Returns whether remove was successful
     */
    public boolean Remove(T x)
    {
        return DoUpdate(Operation.REMOVE, x);
    }

    /***
     * Searches to see if a valu exists in the lfcat
     * @param x value to find
     * @return Returns whether the value was found
     */
    public boolean Lookup(T x)
    {
        BaseNode<T> base = (BaseNode<T>) Util.FindBaseNode(root.get(), (Integer)x);
        boolean found = base.DataContains(x);

        return found;
    }

    /***
     * Returns the avl tree combination of all nodes in the range.
     * @param lowKey low range value
     * @param highKey high range value
     * @return
     */
    public AVLTree<T> RangeQuery(int lowKey, int highKey)
    {
        return RangeNode.AllInRange(root, lowKey, highKey, null);
    }

    /***
     * Performs either the insert or remove
     * @param op Type of operation to perform
     * @param x value to operate with
     * @return Whether operation was succesful
     */
    private boolean DoUpdate(Operation op, T x)
    {
        //starts out with no contention
        ContentionInfo cnt_info = ContentionInfo.UNCONTESTED;
        int key = (Integer) x;
        Node tryBase = null;
        Random random = new Random();

        //number of times contention has been missed
        int missedContention = 0;

        while(true)
        {
            //Finds the available base node tha matches the given key
            BaseNode<T> base = (BaseNode<T>)Util.FindBaseNode(root.get(), (Integer)key);

            //If the base is replaceable then we will attempt to apply the operation to
            //a local copy of the base node and then replace the base node in the actual tree.
            if(base.IsReplaceable())
            {
                boolean res = false;

                //creates local node as a copy of the node in the tree
                BaseNode<T> newb = new BaseNode<T>(base);

                //performs insert in local node
                if (op == Operation.INSERT)
                {
                    res = newb.DataInsert(x);

                }

                //performs remove in local node
                if (op == Operation.REMOVE)
                {
                    res = newb.DataRemove(x);
                }

                //Updates the statistic on the local node based on whether the operation previously failed
                //and found contention.
                newb.statistic = base.NewStat(cnt_info);

                //Tries the replace, if it succeeds then we attempt to manage the lfcat structure with any possible adaptations
                // based on the current contention
                tryBase = Util.TryReplace(root, base, newb);
                if(newb == tryBase)
                {
                    //adapts the successfully replaced node that is now part of the tree
                    newb.AdaptIfNeeded(root);
                    return res;
                }
            }

            //At this point the operation has had a failed attempt so we update the contention count and flag.
            missedContention ++;
            cnt_info = ContentionInfo.CONTESTED;

            //If we recieved a node in CAS exchange, it may be part of an ongoing operation so we can try and help it complete
            if(tryBase != null && tryBase.type != NodeType.BASE && tryBase.type != NodeType.ROUTE)
            {
                tryBase.HelpIfNeeded(root);
            }
            //There are times when the CAS is denied but we have no other node to work with,
            //Multiple nodes could be doing this CAS attempt at roughly the same time, so if they all tried immediately again
            //Then they all could hit high contention again. To avoid this we have added an exponential backoff component
            //that allows the operation to ramdomly back off and stagger retrials a bit.
            else
            {
                try
                {
                    //clamp the contention based off a maximum possible amount
                    int  clamp = missedContention > MAX_BACKOFF ? MAX_BACKOFF : missedContention;
                    //sleep for a 2^contention count + random amount.
                    Thread.sleep(random.nextInt(MAX_BACKOFF_MILLI) +
                            (int)Math.pow(2, clamp));

                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /***
     * Prints the stack recursively (non-concurrent)
     */
    public void Print()
    {
        root.get().Print();
    }

    /***
     * Returns the size of the lfcat (non-concurrent)
     * @return
     */
    public int Size()
    {
        return root.get().Size();
    }
}