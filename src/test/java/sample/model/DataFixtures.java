package sample.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.Setter;
import sample.context.ActionStatusType;
import sample.context.actor.type.ActorRoleType;
import sample.model.account.Account;
import sample.model.account.FiAccount;
import sample.model.account.type.AccountStatusType;
import sample.model.asset.CashBalance;
import sample.model.asset.CashInOut;
import sample.model.asset.Cashflow;
import sample.model.asset.Cashflow.RegCashflow;
import sample.model.asset.type.CashflowType;
import sample.model.master.Holiday;
import sample.model.master.Login;
import sample.model.master.SelfFiAccount;
import sample.model.master.Staff;
import sample.model.master.StaffAuthority;
import sample.util.DateUtils;
import sample.util.TimePoint;

/**
 * Support components for data generation.
 * <p>
 * It is intended for simple master data generation during testing and
 * development and is not intended for production use.
 */
@Setter
public class DataFixtures {

    public void initializeInTxSystem() {
        // String day = DateUtils.dayFormat(LocalDate.now());
        // new AppSetting(Timestamper.KeyDay, "system", "営業日", day).save(repSystem);
    }

    public void initializeInTx() {
        // String ccy = "JPY";
        // LocalDate baseDay = businessDay.day();

        // // 社員: admin (passも同様)
        // staff("admin").save(rep);

        // // 自社金融機関
        // selfFiAcc(Remarks.CashOut, ccy).save(rep);

        // // 口座: sample (passも同様)
        // String idSample = "sample";
        // acc(idSample).save(rep);
        // login(idSample).save(rep);
        // fiAcc(idSample, Remarks.CashOut, ccy).save(rep);
        // cb(idSample, baseDay, ccy, "1000000").save(rep);
    }

    // account

    public static Account acc(String accountId) {
        Account m = new Account();
        m.setAccountId(accountId);
        m.setName(accountId);
        m.setMailAddress(accountId + "@example.com");
        m.setStatusType(AccountStatusType.NORMAL);
        return m;
    }

    public static Login login(PasswordEncoder encoder, String id, ActorRoleType roleType) {
        Login m = new Login();
        m.setActorId(id);
        m.setRoleType(roleType);
        m.setLoginId(id);
        m.setPassword(encoder.encode(id));
        return m;
    }

    public static FiAccount fiAcc(String accountId, String category, String currency) {
        FiAccount m = new FiAccount();
        m.setAccountId(accountId);
        m.setCategory(category);
        m.setCurrency(currency);
        m.setFiCode(category + "-" + currency);
        m.setFiAccountId("FI" + accountId);
        return m;
    }

    // asset

    public CashBalance cb(String accountId, LocalDate baseDay, String currency, String amount) {
        var m = new CashBalance();
        m.setAccountId(accountId);
        m.setBaseDay(baseDay);
        m.setCurrency(currency);
        m.setAmount(new BigDecimal(amount));
        m.setUpdateDate(LocalDateTime.now());
        return m;
    }

    public Cashflow cf(String accountId, String amount, LocalDate eventDay, LocalDate valueDay) {
        return cfReg(accountId, amount, valueDay).create(TimePoint.of(eventDay));
    }

    public RegCashflow cfReg(String accountId, String amount, LocalDate valueDay) {
        return RegCashflow.builder()
                .accountId(accountId)
                .currency("JPY")
                .amount(new BigDecimal(amount))
                .cashflowType(CashflowType.CashIn)
                .remark("cashIn")
                .eventDay(null)
                .valueDay(valueDay)
                .build();
    }

    /**
     * Simplified generation of transfer deposit/withdrawal requests.
     * [Event date (T+1)/Delivery date (T+3)].
     */
    public CashInOut cio(
            BusinessDayHandler businessDay,
            String accountId,
            String absAmount,
            boolean withdrawal) {
        var day = businessDay.day();
        var date = day.atStartOfDay();
        CashInOut m = new CashInOut();
        m.setAccountId(accountId);
        m.setCurrency("JPY");
        m.setAbsAmount(new BigDecimal(absAmount));
        m.setWithdrawal(withdrawal);
        m.setRequestDay(day);
        m.setRequestDate(date);
        m.setEventDay(businessDay.day(1));
        m.setValueDay(businessDay.day(3));
        m.setTargetFiCode("tFiCode");
        m.setTargetFiAccountId("tFiAccId");
        m.setSelfFiCode("sFiCode");
        m.setSelfFiAccountId("sFiAccId");
        m.setStatusType(ActionStatusType.UNPROCESSED);
        return m;
    }

    // master

    public Staff staff(String staffId) {
        var m = new Staff();
        m.setStaffId(staffId);
        m.setName(staffId);
        return m;
    }

    public static List<StaffAuthority> staffAuth(String staffId, String... authority) {
        var idx = new AtomicLong(0);
        return Arrays.stream(authority).map((auth) -> {
            var m = new StaffAuthority();
            m.setId(idx.getAndIncrement());
            m.setStaffId(staffId);
            m.setAuthority(auth);
            return m;
        }).collect(Collectors.toList());
    }

    public static SelfFiAccount selfFiAcc(String category, String currency) {
        SelfFiAccount m = new SelfFiAccount();
        m.setCategory(category);
        m.setCurrency(currency);
        m.setFiCode(category + "-" + currency);
        m.setFiAccountId("xxxxxx");
        return m;
    }

    public static Holiday holiday(String dayStr) {
        Holiday m = new Holiday();
        m.setCategory(Holiday.CategoryDefault);
        m.setName("休日サンプル");
        m.setHoliday(DateUtils.day(dayStr));
        return m;
    }

}
