package sample.model.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import sample.context.support.IdGenerator;

public class IdGeneratorMock implements IdGenerator {
    private final ConcurrentMap<String, AtomicLong> uidMap = new ConcurrentHashMap<>();

    @Override
    public String generate(String key) {
        uidMap.computeIfAbsent(key, k -> new AtomicLong(0));
        return String.valueOf(uidMap.get(key).incrementAndGet());
    }

}
