package com.github.larsderidder.xml;

/**
 * Exception thrown when XML marshaling or unmarshaling fails.
 */
public class XmlMappingException extends RuntimeException {

    public XmlMappingException(String message) {
        super(message);
    }

    public XmlMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
