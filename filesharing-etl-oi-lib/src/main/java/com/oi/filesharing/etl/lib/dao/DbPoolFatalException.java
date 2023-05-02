package com.oi.filesharing.etl.lib.dao;

/**
 * The Class DbPoolFatalException represents a Fatal Error on the Connection Pool Database including one of the following: - Any fail accessing the JDBC Driver - Fail accessing the
 * database - Error, Fail or Deny login in to the database - Fail or Exception creating connections - Fail retrieving connections from the pool - Any SQL Exception Checking the
 * connection - Any generic exception from the Connection Pool.
 */
public class DbPoolFatalException extends Exception {

    private static final long serialVersionUID = -3166372203143222705L;
    private final Exception rootException;

    /**
     * Constructs a new DbPoolFatalException with the specified detail message and RootException. The cause is not initialized, and may subsequently be initialized by a call to
     * {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     * @param rootException
     */
    public DbPoolFatalException(String message, Exception rootException) {
        super(message, rootException);
        this.rootException = rootException;
    }

    /**
     * @return the rootException
     */
    public Exception getRootException() {
        return rootException;
    }
}
