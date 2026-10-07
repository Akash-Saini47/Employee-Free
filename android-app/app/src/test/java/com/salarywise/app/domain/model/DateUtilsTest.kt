package com.salarywise.app.domain.model

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DateUtilsTest {
    @Test fun validDate_parses() {
        assertNotNull(DateUtils.parseDate("07/10/2026"))
    }

    @Test fun invalidDate_isRejected() {
        assertNull(DateUtils.parseDate("31/02/2026"))
    }
}
