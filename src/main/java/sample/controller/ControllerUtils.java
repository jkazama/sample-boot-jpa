package sample.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import sample.context.ValidationException;
import sample.context.report.ReportFile;

/**
 * Controllerで利用されるユーティリティ処理。
 */
public class ControllerUtils {

    /** i18nメッセージ変換を行います。 */
    public static String msg(MessageSource msg, String message, final Locale locale) {
        return msg.getMessage(message, new String[0], locale);
    }

    /**
     * 指定したキー/値をMapに変換します。
     * get等でnullを返す可能性があるときはこのメソッドでMap化してから返すようにしてください。
     * ※nullはJSONバインドされないため、クライアント側でStatusが200にもかかわらず例外扱いされる可能性があります。
     */
    public static <T> Map<String, T> objectToMap(String key, final T t) {
        Map<String, T> ret = new HashMap<>();
        ret.put(key, t);
        return ret;
    }

    public static <T> Map<String, T> objectToMap(final T t) {
        return objectToMap("result", t);
    }

    /** 戻り値を生成して返します。(戻り値がプリミティブまたはnullを許容する時はこちらを利用してください) */
    public static <T> ResponseEntity<T> result(Supplier<T> command) {
        return ResponseEntity.status(HttpStatus.OK).body(command.get());
    }

    public static ResponseEntity<Void> resultEmpty(Runnable command) {
        command.run();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /** ファイルアップロード情報(MultipartFile)をReportFileへ変換します。 */
    public static ReportFile uploadFile(String field, final MultipartFile file) {
        return uploadFile(field, file, (String[]) null);
    }

    /**
     * ファイルアップロード情報(MultipartFile)をReportFileへ変換します。
     * <p>
     * acceptExtensionsに許容するファイル拡張子(小文字統一)を設定してください。
     */
    public static ReportFile uploadFile(String field, final MultipartFile file, final String... acceptExtensions) {
        String fname = StringUtils.lowerCase(file.getOriginalFilename());
        if (acceptExtensions != null && !FilenameUtils.isExtension(fname, acceptExtensions)) {
            throw new ValidationException(
                    field, "アップロードファイルには[{0}]を指定してください",
                    new String[] { StringUtils.join(acceptExtensions, " / ") });
        }
        try {
            return ReportFile.ofByteArray(file.getOriginalFilename(), file.getBytes());
        } catch (IOException e) {
            throw new ValidationException(field, "アップロードファイルの解析に失敗しました");
        }
    }

    /**
     * ファイルダウンロードリソースを返します。
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
