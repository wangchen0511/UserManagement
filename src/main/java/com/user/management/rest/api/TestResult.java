package com.user.management.rest.api;

public class TestResult {

    private String test;
    private boolean isSuccess;
    private String message;

    public TestResult() {}

    public TestResult(final String test, final boolean isSuccess, final String message) {
        this.test = test;
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public String getTest() {
        return test;
    }

    public void setTest(final String test) {
        this.test = test;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(final boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();

        strBuilder.append("test=");
        strBuilder.append(this.test);
        strBuilder.append(",isSuccess=");
        strBuilder.append(this.isSuccess);
        strBuilder.append(",message=");
        strBuilder.append(this.message);

        return strBuilder.toString();
    }

}
