package sample.model;

import sample.ApplicationProperties;
import sample.context.DomainHelper;
import sample.context.Timestamper;
import sample.context.support.AppSettingHandler;
import sample.context.support.IdGenerator;
import sample.model.support.AppSettingHandlerMock;
import sample.model.support.IdGeneratorMock;
import sample.model.support.TimestamperMock;

public class MockDomainHelper implements DomainHelper {
    private final TimestamperMock time = TimestamperMock.of(null, null);
    private final IdGeneratorMock uid = new IdGeneratorMock();
    private final AppSettingHandlerMock setting = new AppSettingHandlerMock();
    private final ApplicationProperties props = new ApplicationProperties();

    @Override
    public Timestamper time() {
        return this.time;
    }

    @Override
    public IdGenerator uid() {
        return this.uid;
    }

    @Override
    public AppSettingHandler setting() {
        return this.setting;
    }

    @Override
    public ApplicationProperties props() {
        return this.props;
    }

}
