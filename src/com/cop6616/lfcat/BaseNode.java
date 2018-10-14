package com.cop6616.lfcat;

import com.samanthadoran.Treap;

public class BaseNode<T extends Comparable<T>> extends Node
{
    private Treap<T> data;

    public BaseNode()
    {
        super(NodeType.NORMAL);
        data = new Treap<T>();
    }

    public BaseNode(Node _parent)
    {
        super(NodeType.NORMAL);
        data = new Treap<T>();
        parent = _parent;
    }

    public boolean DataInsert(T x)
    {
        return data.add(x);
    }

    public boolean DataRemove(T x)
    {
        return data.remove(x);
    }

    public boolean Contains(T x)
    {
        return data.contains(x);
    }

    public int GetDataCount()
    {
        return data.GetSize();
    }
}
