package com.mvc;


public class ControllerNotFoundException extends RuntimeException {
    public ControllerNotFoundException() {
    }

    public ControllerNotFoundException(String message) {
        super(message);
    }
}
