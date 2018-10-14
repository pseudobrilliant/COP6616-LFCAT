package com.cop6616.lfcat;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

public class RouteNode extends Node
{
    int key;

    AtomicReference<Node> left;
    AtomicReference<Node> right;
    AtomicReference<Node> joinID;
    AtomicBoolean valid;

    public RouteNode()
    {
        super(NodeType.ROUTE);
        valid.set(true);
    }

    public RouteNode(int _key, Node _left, Node _right)
    {
        super(NodeType.ROUTE);

        valid.set(true);

        key = _key;

        left.set(_left);
        right.set(_right);

    }

}
