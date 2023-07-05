package sample.context;

/** Message key constants used in exceptions */
public interface ErrorKeys {
    /** There may have been a problem on the server side. */
    String Exception = "error.Exception";
    /** No information found. */
    String EntityNotFound = "error.EntityNotFoundException";
    /** Subject information has been updated by other users. */
    String OptimisticLockingFailure = "error.OptimisticLockingFailure";
    /** Login failed. */
    String Login = "error.Login";
    /** Authentication failed. */
    String Authentication = "error.Authentication";
    /** You are unable to log in. Please contact the administrator. */
    String AuthenticationInvalid = "error.AuthenticationInvalid";
    /** The use of the subject feature is not permitted. */
    String AccessDenied = "error.AccessDeniedException";
    /** An unsupported feature was invoked. */
    String UnsupportedOperation = "error.UnsupportedOperation";

    /** Incorrect body text formatting request accepted. */
    String ServletRequestBinding = "error.ServletRequestBinding";
    /** Incorrect body text formatting request accepted. */
    String HttpMessageNotReadable = "error.HttpMessageNotReadable";
    /** Accepted request for inappropriate media type. */
    String HttpMediaTypeNotAcceptable = "error.HttpMediaTypeNotAcceptable";

}
