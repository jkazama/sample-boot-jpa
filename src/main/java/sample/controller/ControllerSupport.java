package sample.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * UIコントローラの基底クラス。
 */
@Setter
public class ControllerSupport {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MessageSource msg;
    @Autowired
    private ResourceBundleHandler label;
    @Autowired
    private Timestamper time;
    @Autowired
    private ActorSession session;

    /** i18nメッセージ変換を行います。 */
    protected String msg(String message) {
        return msg(message, session.actor().getLocale());
    }

    protected String msg(String message, final Locale locale) {
        return msg.getMessage(message, new String[0], locale);
    }

    /**
     * リソースファイル([basename].properties)内のキー/値のMap情報を返します。
     * <p>API呼び出し側でi18n対応を行いたい時などに利用してください。
     */
    protected Map<String, String> labels(String basename) {
        return labels(basename, session.actor().getLocale());
    }

    protected Map<String, String> labels(String basename, final Locale locale) {
        return label.labels(basename, locale);
    }

    /** メッセージリソースアクセサを返します。 */
    protected MessageSource msgResource() {
        return msg;
    }

    /** 日時ユーティリティを返します。 */
    protected Timestamper time() {
        return time;
    }

    /**
     * 指定したキー/値をMapに変換します。
     * get等でnullを返す可能性があるときはこのメソッドでMap化してから返すようにしてください。
     * ※nullはJSONバインドされないため、クライアント側でStatusが200にもかかわらず例外扱いされる可能性があります。
     */
    protected <T> Map<String, T> objectToMap(String key, final T t) {
        Map<String, T> ret = new HashMap<>();
        ret.put(key, t);
        return ret;
    }

    protected <T> Map<String, T> objectToMap(final T t) {
        return objectToMap("result", t);
    }

    /** 戻り値を生成して返します。(戻り値がプリミティブまたはnullを許容する時はこちらを利用してください) */
    protected <T> ResponseEntity<T> result(Supplier<T> command) {
        return ResponseEntity.status(HttpStatus.OK).body(command.get());
    }

    protected ResponseEntity<Void> resultEmpty(Runnable command) {
        command.run();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /** ファイルアップロード情報(MultipartFile)をReportFileへ変換します。 */
    protected ReportFile uploadFile(final MultipartFile file) {
        return uploadFile(file, (String[]) null);
    }

    /**
     * ファイルアップロード情報(MultipartFile)をReportFileへ変換します。
     * <p>acceptExtensionsに許容するファイル拡張子(小文字統一)を設定してください。
     */
    protected ReportFile uploadFile(final MultipartFile file, final String... acceptExtensions) {
        String fname = StringUtils.lowerCase(file.getOriginalFilename());
        if (acceptExtensions != null && !FilenameUtils.isExtension(fname, acceptExtensions)) {
            throw new ValidationException("file", "アップロードファイルには[{0}]を指定してください",
                    new String[] { StringUtils.join(acceptExtensions) });
        }
        try {
            return new ReportFile(file.getOriginalFilename(), file.getBytes());
        } catch (IOException e) {
            throw new ValidationException("file", "アップロードファイルの解析に失敗しました");
        }
    }

    /**
     * ファイルダウンロード設定を行います。
     * <p>利用する際は戻り値をvoidで定義するようにしてください。
     */
    protected void exportFile(final HttpServletResponse res, final ReportFile file) {
        exportFile(res, file, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    protected void exportFile(final HttpServletResponse res, final ReportFile file, final String contentType) {
        String filename;
        try {
            filename = URLEncoder.encode(file.getName(), "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            throw new ValidationException("ファイル名が不正です");
        }
        res.setContentLength(file.size());
        res.setContentType(contentType);
        res.setHeader("Content-Disposition",
                "attachment; filename=" + filename);
        try {
            IOUtils.write(file.getData(), res.getOutputStream());
        } catch (IOException e) {
            throw new ValidationException("ファイル出力に失敗しました");
        }
    }

}
