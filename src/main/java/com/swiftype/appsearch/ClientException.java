package com.swiftype.appsearch;

/**
 * Base exception class for request errors thrown from the Swiftype App Search Client.
 */
public class ClientException extends Exception {
  private static final long serialVersionUID = -3184706760786998150L;

  ClientException(String message) {
    super(message);
  }

  ClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
