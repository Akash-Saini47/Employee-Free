package com.salarywise.app.domain.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val readableMonthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val shortMonthFormat = SimpleDateFormat("MMM", Locale.getDefault())

    fun formatDate(timestamp: Long): String {
        return displayFormat.format(Date(timestamp))
    }

    fun parseDate(dateStr: String): Long? {
        return try {
            displayFormat.parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentMonthYear(): String {
        return monthYearFormat.format(Date())
    }

    fun formatMonthYear(monthYear: String): String {
        return try {
            val date = monthYearFormat.parse(monthYear)
            if (date != null) readableMonthFormat.format(date) else monthYear
        } catch (e: Exception) {
            monthYear
        }
    }

    fun getShortMonthName(monthYear: String): String {
        return try {
            val date = monthYearFormat.parse(monthYear)
            if (date != null) shortMonthFormat.format(date) else monthYear
        } catch (e: Exception) {
            monthYear
        }
    }

    fun getPreviousMonthYear(monthYear: String): String {
        return try {
            val cal = Calendar.getInstance()
            val date = monthYearFormat.parse(monthYear)
            if (date != null) {
                cal.time = date
                cal.add(Calendar.MONTH, -1)
                monthYearFormat.format(cal.time)
            } else {
                monthYear
            }
        } catch (e: Exception) {
            monthYear
        }
    }

    fun getStartAndEndOfDay(cal: Calendar = Calendar.getInstance()): Pair<Long, Long> {
        val start = cal.clone() as Calendar
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)

        val end = cal.clone() as Calendar
        end.set(Calendar.HOUR_OF_DAY, 23)
        end.set(Calendar.MINUTE, 59)
        end.set(Calendar.SECOND, 59)
        end.set(Calendar.MILLISECOND, 999)

        return Pair(start.timeInMillis, end.timeInMillis)
    }

    fun getStartAndEndOfWeek(cal: Calendar = Calendar.getInstance()): Pair<Long, Long> {
        val start = cal.clone() as Calendar
        start.set(Calendar.DAY_OF_WEEK, start.firstDayOfWeek)
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)

        val end = start.clone() as Calendar
        end.add(Calendar.DAY_OF_WEEK, 6)
        end.set(Calendar.HOUR_OF_DAY, 23)
        end.set(Calendar.MINUTE, 59)
        end.set(Calendar.SECOND, 59)
        end.set(Calendar.MILLISECOND, 999)

        return Pair(start.timeInMillis, end.timeInMillis)
    }

    fun getStartAndEndOfMonth(monthYear: String): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        try {
            val date = monthYearFormat.parse(monthYear)
            if (date != null) cal.time = date
        } catch (e: Exception) {}

        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }

    fun getRangeForMonthsAgo(monthsAgo: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val end = cal.timeInMillis

        cal.add(Calendar.MONTH, -monthsAgo)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        return Pair(start, end)
    }

    fun getStartAndEndOfCurrentYear(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.MONTH, Calendar.DECEMBER)
        cal.set(Calendar.DAY_OF_MONTH, 31)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis

        return Pair(start, end)
    }
}
