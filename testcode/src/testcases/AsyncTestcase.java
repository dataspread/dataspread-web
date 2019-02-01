package testcases;

public interface AsyncTestcase {
    /**
     * Change the value of one cell (now must be A1(0,0)).
     */
    void change();

    /**
     * Make getValue() calls to all cells in order to make sure that
     * lazy computation is triggered for them.
     */
    void touchAll();

    /**
     * Verify the evaluated values of cells after calling change()
     * @return true if evaluated values are correct
     */
    boolean verify();
}
