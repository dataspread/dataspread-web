package ASyncTest.tests;

public interface AsyncTestFactory {

    /**
     * @return A fresh copy of the current test case with its
     * test sheet and test book initialized.
     */
    AsyncBaseTest createTest();

    /**
     * @return The human-readable string representation of
     * this test. Tests with the same test parameters should
     * have the same string representation. If this is not
     * the case, file naming errors may occur.
     */
    @Override
    String toString();

}
