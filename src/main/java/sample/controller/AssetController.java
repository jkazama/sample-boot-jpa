package sample.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import sample.context.ActionStatusType;
import sample.context.Dto;
import sample.model.asset.CashInOut;
import sample.usecase.AssetService;
import sample.usecase.AssetService.UserRegCashOut;

/**
 * API controller of the asset domain.
 */
@RestController
@RequestMapping("/api/asset")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService service;

    @GetMapping("/cio/unprocessedOut")
    public List<UserCashOut> findUnprocessedCashOut() {
        return service.findUnprocessedCashOut().stream()
                .map(UserCashOut::of)
                .toList();
    }

    @PostMapping("/cio/withdraw")
    public Map<String, String> withdraw(@RequestBody @Valid UserRegCashOut param) {
        return ControllerUtils.objectToMap("id", service.withdraw(param));
    }

    @Builder
    public static record UserCashOut(
            String cashInOutId,
            String currency,
            BigDecimal absAmount,
            LocalDate requestDay,
            LocalDateTime requestDate,
            LocalDate eventDay,
            LocalDate valueDay,
            ActionStatusType statusType,
            LocalDateTime updateDate,
            String cashflowId) implements Dto {
        public static UserCashOut of(final CashInOut cio) {
            return UserCashOut.builder()
                    .cashInOutId(cio.getCashInOutId())
                    .currency(cio.getCurrency())
                    .absAmount(cio.getAbsAmount())
                    .requestDay(cio.getRequestDay())
                    .requestDate(cio.getRequestDate())
                    .eventDay(cio.getEventDay())
                    .valueDay(cio.getValueDay())
                    .statusType(cio.getStatusType())
                    .updateDate(cio.getUpdateDate())
                    .cashflowId(cio.getCashflowId())
                    .build();
        }
    }

}
