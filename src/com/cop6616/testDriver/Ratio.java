package com.cop6616.testDriver;

public class Ratio
{
    float rangeOperations = 0.0f;
    float individualOperations = 0.0f;
    float insertOperations = 0.0f;
    float removeOperations = 0.0f;
    float containsOperations = 0.0f;

    private int rangeCount = 0;
    private int insertCount = 0;
    private int removeCount = 0;
    private int containsCount = 0;
    private int totalCount = 0;

    Ratio()
    {
    }

    Ratio(float _rangeOperations, float _insertOperations, float _removeOperations, float _containsOperations)
    {
        rangeOperations = _rangeOperations;
        insertOperations = _insertOperations;
        removeOperations = _removeOperations;
        containsOperations = _containsOperations;

        individualOperations = insertOperations + removeOperations + containsOperations;
    }

    public void  UpdateRatios()
    {
        rangeOperations = (float)rangeCount / (float)totalCount;
        insertOperations = (float)insertCount / (float)totalCount;
        removeOperations = (float)removeCount / (float)totalCount;
        containsOperations = (float)containsCount / (float)totalCount;

        individualOperations = (insertOperations + removeOperations + containsOperations) / (float)totalCount;
    }


    public void UpdateRangeCount(int delta)
    {
        rangeCount += delta;
        totalCount += delta;

        UpdateRatios();

    }

    public void UpdateInsertCount(int delta)
    {
        insertCount += delta;
        totalCount += delta;

        UpdateRatios();
    }

    public void UpdateRemoveCount(int delta)
    {
        removeCount += delta;
        totalCount += delta;

        UpdateRatios();
    }

    public void UpdateContainsCount(int delta)
    {
        containsCount += delta;
        totalCount += delta;

        UpdateRatios();
    }
}