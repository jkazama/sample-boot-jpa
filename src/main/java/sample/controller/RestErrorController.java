package sample.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * Controller which performs exception handling for RestController.
 * <p>Enable it in combination with "error.path" attribute of application.yml.
 * It is necessary to destroy whitelabel in total. "error.whitelabel.enabled: false"
 * <p>see ErrorMvcAutoConfiguration
 */
@RestController
public class RestErrorController implements ErrorController {
    public static final String PathError = "/api/error";

    @Autowired
    private ErrorAttributes errorAttributes;

    @Override
    public String getErrorPath() {
        return PathError;
    }

    @RequestMapping(PathError)
    public Map<String, Object> error(ServletWebRequest request) {
        return this.errorAttributes.getErrorAttributes(request, false);
    }

}
