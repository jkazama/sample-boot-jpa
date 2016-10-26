package sample.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.Setter;
import sample.ValidationException;
import sample.context.ResourceBundleHandler;
import sample.context.Timestamper;
import sample.context.actor.ActorSession;
import sample.context.report.ReportFile;

/**
 * The base class of the UI controller.
 */
@Setter
public class ControllerSupport {

    @Autowired
    private MessageSource msg;
    @Autowired
    private ResourceBundleHandler label;
    @Autowired
    private Timestamper time;
    @Autowired
    private ActorSession session;

    protected String msg(String message) {
        return msg(message, session.actor().getLocale());
    }

    protected String msg(String message, final Locale locale) {
        return msg.getMessage(message, new String[0], locale);
    }

    /**
     * Give back the Map information of the key / level in resource file ([basename].properties).
     * <p>When i18n wants to support with API caller, please use it.
     */
    protected Map<String, String> labels(String basename) {
        return labels(basename, session.actor().getLocale());
    }

    protected Map<String, String> labels(String basename, final Locale locale) {
        return label.labels(basename, locale);
    }

    protected MessageSource msgResource() {
        return msg;
    }

    protected Timestamper time() {
        return time;
    }

    protected <T> Map<String, T> objectToMap(String key, final T t) {
        Map<String, T> ret = new HashMap<>();
        ret.put(key, t);
        return ret;
    }

    protected <T> Map<String, T> objectToMap(final T t) {
        return objectToMap("result", t);
    }

    protected <T> ResponseEntity<T> result(Supplier<T> command) {
        return ResponseEntity.status(HttpStatus.OK).body(command.get());
    }

    protected ResponseEntity<Void> resultEmpty(Runnable command) {
        command.run();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    protected ReportFile uploadFile(final MultipartFile file) {
        return uploadFile(file, (String[]) null);
    }

    protected ReportFile uploadFile(final MultipartFile file, final String... acceptExtensions) {
        String fname = StringUtils.lowerCase(file.getOriginalFilename());
        if (acceptExtensions != null && !FilenameUtils.isExtension(fname, acceptExtensions)) {
            throw new ValidationException("file", "Please set [{0}] in an upload file",
                    new String[] { StringUtils.join(acceptExtensions) });
        }
        try {
            return new ReportFile(file.getOriginalFilename(), file.getBytes());
        } catch (IOException e) {
            throw new ValidationException("file", "Failed in the uptake of the upload file.");
        }
    }

    /**
     * Set file downloading.
     * <p>Define the return value in void.
     */
    protected void exportFile(final HttpServletResponse res, final ReportFile file) {
        exportFile(res, file, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    protected void exportFile(final HttpServletResponse res, final ReportFile file, final String contentType) {
        String filename;
        try {
            filename = URLEncoder.encode(file.getName(), "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            throw new ValidationException("A file name is unjust.");
        }
        res.setContentLength(file.size());
        res.setContentType(contentType);
        res.setHeader("Content-Disposition",
                "attachment; filename=" + filename);
        try {
            IOUtils.write(file.getData(), res.getOutputStream());
        } catch (IOException e) {
            throw new ValidationException("Failed in a file export.");
        }
    }

}
