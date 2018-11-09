package sample.controller.system;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sample.controller.ControllerUtils;
import sample.usecase.*;

/**
 * システムジョブのUI要求を処理します。
 * <p>/api/system 以降の URL はジョブスケジューラから実行される事を想定しているため、 L/B 等で外部からアクセス不可にしておく必要があります。
 * ( よりベターなアプローチは該当処理のみを持ったバッチプロセスとして切り出すか個別認証をかける )
 * low: 通常はバッチプロセス(または社内プロセスに内包)を別途作成して、ジョブスケジューラから実行される方式になります。
 * ジョブの負荷がオンライン側へ影響を与えないよう事前段階の設計が重要になります。
 * low: 社内/バッチプロセス切り出す場合はVM分散時の情報/排他同期を意識する必要があります。(DB同期/メッセージング同期/分散製品の利用 等)
 */
@RestController
@RequestMapping("/api/system/job")
public class JobController {

    private final AssetAdminService asset;
    private final SystemAdminService system;
    
    public JobController(
            AssetAdminService asset,
            SystemAdminService system) {
        this.asset = asset;
        this.system = system;
    }

    /** 営業日を進めます。 */
    @PostMapping("/daily/processDay")
    public ResponseEntity<Void> processDay() {
        return ControllerUtils.resultEmpty(() -> system.processDay());
    }

    /** 振込出金依頼を締めます。 */
    @PostMapping("/daily/closingCashOut")
    public ResponseEntity<Void> closingCashOut() {
        return ControllerUtils.resultEmpty(() -> asset.closingCashOut());
    }

    /** キャッシュフローを実現します。 */
    @PostMapping("/daily/realizeCashflow")
    public ResponseEntity<Void> realizeCashflow() {
        return ControllerUtils.resultEmpty(() -> asset.realizeCashflow());
    }

}
