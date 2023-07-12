package sample.model;

import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sample.context.DomainHelper;
import sample.context.support.IdGenerator;
import sample.model.asset.CashInOut;

/**
 * Domain-specific implementation of IdGenerator
 */
@Component
@RequiredArgsConstructor(staticName = "of")
public class DomainIdGenerator implements IdGenerator {
    private final DomainHelper dh;
    private final Map<String, Function<Long, String>> uidMap = Map.of(
            CashInOut.class.getSimpleName(), id -> CashInOut.formatId(id));

    /** {@inheritDoc} */
    @Override
    public String generate(String key) {
        if (!uidMap.containsKey(key)) {
            throw new IllegalArgumentException("Unsupported generation key. [" + key + "]");
        }
        return uidMap.get(key).apply(dh.setting().nextId(key));
    }

}
