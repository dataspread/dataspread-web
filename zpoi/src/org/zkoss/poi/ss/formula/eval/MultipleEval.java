package org.zkoss.poi.ss.formula.eval;

/**
 * This class is a marker class. It is a special value for multiple area cells.
 */

public final class MultipleEval implements ValueEval {

    private AreaEval _value;
    private int _width;
    private int _height;

    MultipleEval()
    {

    }

    public MultipleEval(int width, int height, AreaEval value) {
        _value = value;
        _width = width;
        _height = height;
    }

    public AreaEval getValue() {
        return _value;
    }

    public int getWidth() {
        return _width;
    }

    public int getHeight() {
        return _height;
    }

}