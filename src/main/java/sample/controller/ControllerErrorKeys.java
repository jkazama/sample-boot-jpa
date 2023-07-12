package sample.controller;

/**
 * Message key constants used in the controller.
 */
public interface ControllerErrorKeys {
    /** key prefix */
    String Prefix = "error.controller.";

    /** Please specify [{0}] for the uploaded file. */
    String UploadFileExtension = "error.controller.uploadFileExtension";
    /** Failed to parse uploaded file. */
    String UploadFileParse = "error.controller.uploadFileParse";

}
