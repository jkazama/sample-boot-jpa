package sample.model;

/**
 * Message key constants used in the generic domain.
 */
public interface DomainErrorKeys {
    /** key prefix */
    String Prefix = "error.domain.";

    /** It is in a state that cannot be changed. */
    String StatusType = Prefix + "statusType";
    /** Cannot delete because related information exists. */
    String DeleteDependencies = Prefix + "deleteDependencies";

    /** Already registered ID. */
    String DuplicateId = Prefix + "duplicateId";
    /** The name is already registered. */
    String DuplicateName = Prefix + "duplicateName";
    /** This information is already registered. */
    String DuplicateEntity = Prefix + "duplicateEntity";

    /** must be the current date or later. */
    String AfterEqualsToday = Prefix + "afterEqualsToday";
    /** must be a later date than the current date. */
    String AfterToday = Prefix + "afterToday";
    /** must be the current date or earlier. */
    String BeforeEqualsToday = Prefix + "beforeEqualsToday";
    /** must be the date prior to the current date. */
    String BeforeToday = Prefix + "beforeToday";
    /** incorrect date sequence. */
    String BetweenDay = Prefix + "betweenDay";

    /** must be zero or positive number. */
    String AbsAmountZero = Prefix + "absAmountZero";

}
