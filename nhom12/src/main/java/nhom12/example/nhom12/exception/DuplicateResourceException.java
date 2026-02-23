package nhom12.example.nhom12.exception;

public class DuplicateResourceException extends RuntimeException {

  public DuplicateResourceException(String resource, String field, String value) {
    super(String.format("%s already exists with %s: '%s'", resource, field, value));
  }
}
