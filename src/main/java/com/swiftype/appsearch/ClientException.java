package com.swiftype.appsearch;

/**
 * Base exception class for request errors thrown from the Swiftype App Search Client.
 */
public class ClientException extends Exception {
  private static final long serialVersionUID = -3184706760786998150L;

  private int code;

  ClientException(String message) {
    super(message);
  }

  ClientException(int code, String message) {
    super(message);
    this.code = code;
  }

  ClientException(String message, Throwable cause) {
    super(message, cause);
  }

  public int getCode() {
    return code;
  }
}
