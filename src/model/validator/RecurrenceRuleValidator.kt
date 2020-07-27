package ombruk.backend.model.validator

import ombruk.backend.model.RecurrenceRule

object RecurrenceRuleValidator : ModelValidator<RecurrenceRule> {
    override fun validate(rule: RecurrenceRule): ModelValidatorCode {
        if (rule.count != null && rule.until != null) return RecurrenceRuleValidatorCode.BothUntilAndCount
        if (rule.count == null && rule.until == null) return RecurrenceRuleValidatorCode.UnspecifiedEnding
        if (rule.interval <= 0) return RecurrenceRuleValidatorCode.InvalidInterval
        return RecurrenceRuleValidatorCode.OK
    }
}

enum class RecurrenceRuleValidatorCode : ModelValidatorCode {
    OK {
        override val info: String? = null
    },
    BothUntilAndCount {
        override val info = "Both until and count can't be specified"
    },
    InvalidInterval {
        override val info = "Interval must be 1 or more if specified"
    },
    UnspecifiedEnding {
        override val info = "Either until or count needs to be specified"
    }
}
