package sample.usecase.report;

import java.io.InputStream;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.Setter;
import sample.context.orm.DefaultRepository;
import sample.context.report.ReportHandler;
import sample.model.asset.CashInOut.FindCashInOut;
import sample.usecase.ServiceUtils;

/**
 * アプリケーション層のレポート出力を行います。
 * <p>独自にトランザクションを管理するので、サービスのトランザクション内で
 * 呼び出さないように注意してください。
 * low: コード量が多くなるため今回のサンプルでは対象外とします。
 */
@Component
@Setter
public class ServiceReportExporter {

    @Autowired
    private DefaultRepository rep;
    @Autowired
    @Qualifier(DefaultRepository.BeanNameTx)
    private PlatformTransactionManager tx;
    @Autowired
    private ReportHandler report;

    /** トランザクション処理を実行します。 */
    private <T> T tx(Supplier<T> callable) {
        return ServiceUtils.tx(tx, callable);
    }

    /** トランザクション処理を実行します。 */
    private void tx(Runnable command) {
        ServiceUtils.tx(tx, command);
    }

    /**　振込入出金情報をCSV出力します。 */
    public byte[] exportCashInOut(final FindCashInOut p) {
        //low: バイナリ生成。条件指定を可能にしたオンラインダウンロードを想定。
        return new byte[0];
    }

    public void exportAnyBigData(final InputStream ins, final FindCashInOut p) {
        //low: サイズが多いケースではストリームへ都度書き出しする。
    }

    /**　振込入出金情報を帳票出力します。 */
    public void exportFileCashInOut(String baseDay) {
        //low: 特定のディレクトリへのファイル出力。ジョブ等での利用を想定
    }

    public int callbackSample() {// for warning
        return tx(() -> {
            report.hashCode();
            return rep.hashCode();
        });
    }

    public void commandSample() {// for warning
        tx(() -> {
            rep.hashCode();
        });
    }

}
