package com.cop6616.lfcat;

import com.jwetherell.algorithms.data_structures.AVLTree;
import java.util.concurrent.atomic.AtomicReference;

/***
 * Base Node class used to store data as leaf nodes in the LFCAT.
 * All data is stored in AVL trees within the base node, as such functions are provided to manage the avl data.
 * Also provides LFCAT Node functions to update stats based on use and adapt to high or low contention on itself.
 * @param <T>
 */
public class BaseNode<T extends Comparable<T>> extends Node
{
    AVLTree<T> data;

    protected static final int MAX_SIZE_CNTB = 100;
    public static int treeSize = 100;

    /***
     * Default constructor for Base Nodes
     */
    public BaseNode()
    {
        type = NodeType.BASE;
        data = new AVLTree<T>();
    }

    /***
     * Copy constructor for base nodes that take in another avl tree.
     * @param _data
     */
    public BaseNode(AVLTree<T> _data)
    {
        type = NodeType.BASE;
        data = new AVLTree<T>(_data);
    }


    /***
     * Copy constructor for base nodes that take in another avl tree.
     * @param _node
     */
    public BaseNode(BaseNode<T> _node)
    {
        type = NodeType.BASE;
        parent = _node.parent;
        data = new AVLTree<T>(_node.data);
    }

    /***
     * Takes in the current level of contention experienced by an operation and updates the stat accordingly.
     * Stat is also updated depending on a size threshold for the amount of data that can be stored on the node.
     * If the avltree contains too many nodes, traversing/managing the tree becomes problematic so we apply the size
     * of the tree as management adaptation.
     * @param cnt
     * @return
     */
    public int NewStat(ContentionInfo cnt)
    {
        int stat = super.NewStat(cnt);

        if(data.size() >= treeSize)
        {
            stat += MAX_SIZE_CNTB;
        }

        return stat;
    }

    /***
     * Inserts x into the Base Node's AVL tree data structure
     * @param x Value to insert
     * @return Whether value was successfully inserted
     */
    public boolean DataInsert(T x)
    {
        return data.add(x);
    }

    /***
     * Removes x from the Base Node's AVL tree data structure
     * @param x Value to remove
     * @return Whether value was successfully removed
     */
    public boolean DataRemove(T x)
    {
        return data.remove(x) == x;
    }

    /***
     * Sees if the Base Node's AVL tree data structure contains x
     * @param x Value to search
     * @return Whether value was contained in the AVL structure
     */
    public boolean DataContains(T x)
    {
        return data.contains(x);
    }

    /***
     * @return Size of the AVL data structure
     */
    public int DataCount()
    {
        return data.size();
    }

    /***
     * All Base nodes are in a fixed immutable state. Meaning that they can be replaced at any time.
     * @return Always returns true
     */
    public boolean IsReplaceable ()
    {
            return true;
    }

    /***
     * All Base nodes are in a fixed immutable state. Meaning that they do not need assistance.
     * @return
     */
    public void HelpIfNeeded(AtomicReference<Node> root)
    {
      return;
    }

    /***
     * Checks the contention statistic of a node and splits or joins as needed
     */
    public void AdaptIfNeeded(AtomicReference<Node> root)
    {
        //If we cannot replace the node the we cannot adapt it
        if(!IsReplaceable())
        {
            return;
        }

        //If the statistic passes the high contention threshold then it is experiencing high contention
        //The node is then split to ease that contentions.
        if(this.NewStat(ContentionInfo.NOINFO) > HIGH_CONT)
        {
            SplitAdaptation(root, this);
        }
        //If the statistic passes the low contention threshold then it is experiencing low contention
        //The node is then joined with another available node to help operations that require a larger available range.
        else if(this.NewStat(ContentionInfo.NOINFO) < LOW_CONT)
        {
            JoinAdaptation(root, this);
        }
    }

    
    /***
     * If there is high level of contention, then there are numerous individual operations that are currently failing.
     * To alleviate this contention the LFCAT can split base node apart into smaller ranges. A route node is then used
     * to link these two new base nodes and make them part of the LFCAT. this is function is marked as static so that
     * it can function of the LFCAT separte from the actual nodes.
     * @param root The root node in the eyes of the current operation
     * @param b The node at which to split the data
     * @param <T> Type of values stored by the LFCAT nodes.
     */
    public static <T extends Comparable<T>> void SplitAdaptation(AtomicReference<Node> root, BaseNode b)
    {
        if(b.DataCount() >= 2)
        {
            //split the base node into a pair of AVL tress
            AVLTree.Pair<T> pair = b.data.split();

            BaseNode<T> left = new BaseNode<T>(pair.getLesser());

            BaseNode<T> right = new BaseNode<T>(pair.getGreater());

            //Create a new route to link the new left and right base nodes back to the LFCAT
            RouteNode r = new RouteNode((Integer)pair.key, left, right);

            left.parent = r;

            right.parent = r;

            //Attempts to finalize the split by replacing the root taget node (originally the base node) with the new route node
            Util.TryReplace(root, b, r);
        }
    }

    /***
     * @return Returns the size of the avl tree as the amount of sata contained by the base node
     */
    public int Size()
    {
        return data.size();
    }

    /***
     * Joins a node with its right- or left-hand neighbor if there is low contention
     * @param root The root node in the eyes of the current operation
     * @param b The node at which to join the data
     */
    public static <T extends Comparable<T>> void JoinAdaptation(AtomicReference<Node> root, Node b)
    {
        //Cannot join nodes if already the only node
        if(b.parent == null)
        {
            return;
        }

        //If the target node is a left child, attempt a left join and complete
        if( ((RouteNode) b.parent).left.get() == b )
        {
            Node m = JoinMainNode.SecureJoinLeft(root, b);
            if (m != null)
            {
                JoinMainNode.CompleteJoin(root, m);
            }
        }
        //If the target node is a left child, attempt a right join and complete
        else if ( ((RouteNode) b.parent).right.get() == b )
        {
            Node m = JoinMainNode.SecureJoinRight(root, b);
            if (m != null)
            {
                JoinMainNode.CompleteJoin(root, m);
            }
        }
    }

    public void Print()
    {
        int size = data.size();
        int min = (Integer)data.min();
        Integer max = (Integer)data.max();

        System.out.println("Base Node - Size: " + size +" Min: " + min + " Max: " + max );
    }
}
