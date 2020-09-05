package com.swiftype.appsearch;

public class InvalidDocumentException extends ClientException {
  private static final long serialVersionUID = -2237576465535255261L;

  InvalidDocumentException(String message) { super(message); }
}
