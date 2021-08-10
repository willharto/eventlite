package uk.ac.man.cs.eventlite.dao;

public class ServiceException extends Exception {
    public ServiceException() {
        super();
    }

    public ServiceException(String message) {
        super(message);
    }
}
