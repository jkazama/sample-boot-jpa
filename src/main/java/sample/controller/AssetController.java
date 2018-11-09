package sample.controller;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.Value;
import sample.ActionStatusType;
import sample.context.Dto;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.RegCashOut;
import sample.usecase.AssetService;

/**
 * 資産に関わる顧客のUI要求を処理します。
 */
@RestController
@RequestMapping("/api/asset")
public class AssetController {

    private final AssetService service;
    public AssetController(AssetService service) {
        this.service = service;
    }

    /** 未処理の振込依頼情報を検索します。 */
    @GetMapping("/cio/unprocessedOut/")
    public List<CashOutUI> findUnprocessedCashOut() {
        return service.findUnprocessedCashOut().stream().map((cio) -> CashOutUI.of(cio)).collect(Collectors.toList());
    }

    /**
     * 振込出金依頼をします。
     * low: RestControllerの標準の振る舞いとしてvoidやプリミティブ型はJSON化されないので注意してください。
     * (解析時の優先順位の関係だと思いますが)
     */
    @PostMapping("/cio/withdraw")
    public ResponseEntity<Long> withdraw(@Valid RegCashOut p) {
        return ControllerUtils.result(() -> service.withdraw(p));
    }

    /** 振込出金依頼情報の表示用Dto */
    @Value
    public static class CashOutUI implements Dto {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String currency;
        private BigDecimal absAmount;
        private LocalDate requestDay;
        private LocalDateTime requestDate;
        private LocalDate eventDay;
        private LocalDate valueDay;
        private ActionStatusType statusType;
        private LocalDateTime updateDate;
        private Long cashflowId;

        public static CashOutUI of(final CashInOut cio) {
            return new CashOutUI(cio.getId(), cio.getCurrency(), cio.getAbsAmount(), cio.getRequestDay(),
                    cio.getRequestDate(), cio.getEventDay(), cio.getValueDay(), cio.getStatusType(),
                    cio.getUpdateDate(), cio.getCashflowId());
        }
    }

}
