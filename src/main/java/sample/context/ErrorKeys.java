package sample.context;

/** Message key constants used in exceptions */
public interface ErrorKeys {
    String Prefix = "error.";

    /** There may have been a problem on the server side. */
    String Exception = Prefix + "Exception";
    /** No information found. */
    String EntityNotFound = Prefix + "EntityNotFoundException";
    /** Subject information has been updated by other users. */
    String OptimisticLockingFailure = Prefix + "OptimisticLockingFailure";
    /** Login failed. */
    String Login = Prefix + "Login";
    /** Authentication failed. */
    String Authentication = Prefix + "Authentication";
    /** You are unable to log in. Please contact the administrator. */
    String AuthenticationInvalid = Prefix + "AuthenticationInvalid";
    /** The use of the subject feature is not permitted. */
    String AccessDenied = Prefix + "AccessDeniedException";
    /** An unsupported feature was invoked. */
    String UnsupportedOperation = Prefix + "UnsupportedOperation";

    /** Incorrect body text formatting request accepted. */
    String ServletRequestBinding = Prefix + "ServletRequestBinding";
    /** Incorrect body text formatting request accepted. */
    String HttpMessageNotReadable = Prefix + "HttpMessageNotReadable";
    /** Accepted request for inappropriate media type. */
    String HttpMediaTypeNotAcceptable = Prefix + "HttpMediaTypeNotAcceptable";

}
