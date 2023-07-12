package sample.context.report;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import lombok.Builder;
import sample.context.Dto;

/** Represents a report file image. */
@Builder
public record ReportFile(
        String name,
        String contentType,
        Resource data) implements Dto {

    public Optional<Long> size() {
        try {
            return Optional.of(data.contentLength());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public boolean isInputStream() {
        return this.data instanceof InputStreamResource;
    }

    public ReportFile copy(String name) {
        return new ReportFile(name, contentType, data);
    }

    public static ReportFile ofByteArray(String name, byte[] data) {
        return ReportFile.builder()
                .name(name)
                .data(new ByteArrayResource(data))
                .build();
    }

    public static ReportFile ofByteArray(String name, String contentType, byte[] data) {
        return ReportFile.builder()
                .name(name)
                .contentType(contentType)
                .data(new ByteArrayResource(data))
                .build();
    }

    public static ReportFile ofInputStream(String name, InputStream data) {
        return ReportFile.builder()
                .name(name)
                .data(new InputStreamResource(data))
                .build();
    }

    public static ReportFile ofInputStream(String name, String contentType, InputStream data) {
        return ReportFile.builder()
                .name(name)
                .contentType(contentType)
                .data(new InputStreamResource(data))
                .build();
    }

}
