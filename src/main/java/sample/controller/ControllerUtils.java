package sample.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import sample.context.ValidationException;
import sample.context.report.ReportFile;

/**
 * Utility processes used by the Controller.
 */
public abstract class ControllerUtils {

    /**
     * Converts the specified key/value to a Map.
     * <p>
     * If there is a possibility of returning null in get, etc., use this method to
     * map the data before returning it.
     * null is not JSON bound and may be treated as an exception even though
     * Status
     * is 200 on the client side.
     */
    public static <T> Map<String, T> objectToMap(String key, final T t) {
        Map<String, T> ret = new HashMap<>();
        ret.put(key, t);
        return ret;
    }

    public static <T> Map<String, T> objectToMap(final T t) {
        return objectToMap("result", t);
    }

    /**
     * Generate and return ResponseEntity. (Use this when the return value is
     * allowed to be primitive or null.)
     */
    public static <T> ResponseEntity<T> result(Supplier<T> command) {
        return ResponseEntity.status(HttpStatus.OK).body(command.get());
    }

    public static ResponseEntity<Void> resultEmpty(Runnable command) {
        command.run();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /** Converts file upload information (MultipartFile) to ReportFile. */
    public static ReportFile uploadFile(String field, final MultipartFile file) {
        return uploadFile(field, file, (String[]) null);
    }

    /**
     * Converts file upload information (MultipartFile) to ReportFile.
     * <p>
     * Set the acceptable file extensions (lower case unified) in acceptExtensions.
     */
    public static ReportFile uploadFile(String field, final MultipartFile file, final String... acceptExtensions) {
        String fname = StringUtils.lowerCase(file.getOriginalFilename());
        if (acceptExtensions != null && !FilenameUtils.isExtension(fname, acceptExtensions)) {
            throw ValidationException.ofField(
                    field,
                    ControllerErrorKeys.UploadFileExtension,
                    StringUtils.join(acceptExtensions, " / "));
        }
        try {
            return ReportFile.ofByteArray(file.getOriginalFilename(), file.getBytes());
        } catch (IOException e) {
            throw ValidationException.ofField(field, ControllerErrorKeys.UploadFileParse);
        }
    }

    /**
     * Returns the file download resource.
     */
    public static ResponseEntity<Resource> exportFile(Supplier<ReportFile> fileFn) {
        return exportFile(fileFn, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    public static ResponseEntity<Resource> exportFile(Supplier<ReportFile> fileFn, String contentType) {
        ReportFile file = fileFn.get();
        String filename;
        try {
            filename = URLEncoder.encode(file.name(), "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            filename = file.name();
        }
        var result = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.CONTENT_TYPE, contentType);
        Optional<Long> contentLength = file.size();
        contentLength.ifPresent((len) -> {
            result.header(HttpHeaders.CONTENT_LENGTH, String.valueOf(len));
        });
        return result.body(file.data());
    }

}
