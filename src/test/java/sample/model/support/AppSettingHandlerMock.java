package sample.model.support;

import java.util.HashMap;
import java.util.Map;

import sample.context.support.AppSetting;
import sample.context.support.AppSettingHandler;

public class AppSettingHandlerMock implements AppSettingHandler {
    private final Map<String, String> valueMap = new HashMap<>();

    @Override
    public AppSetting setting(String id) {
        this.valueMap.putIfAbsent(id, null);
        return AppSetting.of(id, this.valueMap.get(id));
    }

    @Override
    public AppSetting change(String id, String value) {
        this.valueMap.put(id, value);
        return this.setting(id);
    }

    @Override
    public long nextId(String id) {
        this.valueMap.putIfAbsent(id, "0");
        var v = Long.valueOf(this.valueMap.get(id)) + 1;
        this.valueMap.put(id, String.valueOf(v));
        return v;
    }

}
