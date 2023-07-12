package sample.model.asset;

import sample.model.DomainErrorKeys;

/**
 * Message key constants used in the asset domain.
 */
public interface AssetErrorKeys {
    /** key prefix */
    String Prefix = DomainErrorKeys.Prefix + "asset.";

    /** This cannot be realized because the delivery date has not been reached. */
    String RealizeDay = Prefix + "realizeDay";
    /** The delivery date has already been reached. */
    String AfterValueDay = Prefix + "afterValueDay";

    /** The date of accrual that has not yet arrived. */
    String BeforeEventDay = Prefix + "beforeEventDay";
    /** The date of accrual has already been reached. */
    String AfterEqualsEventDay = Prefix + "afterEqualsEventDay";
    /** The amount available for withdrawal has been exceeded. */
    String WithdrawAmount = Prefix + "withdrawAmount";

}
