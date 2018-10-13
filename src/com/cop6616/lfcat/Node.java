package com.cop6616.lfcat;

enum NodeType
{
    ROUTE,
    NORMAL,
    JOIN_MAIN,
    JOIN_NEIGHBOR,
    RANGE
}

public class Node
{
    NodeType type;
}
