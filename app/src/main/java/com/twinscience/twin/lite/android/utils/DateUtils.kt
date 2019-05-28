package com.twinscience.twin.lite.android.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by mertselcukdemir on 25.06.2018
 * Copyright (c) 2018 YGA to present
 * All rights reserved.
 */
object DateUtils {

    fun convertCalendarToString() {

    }

    fun convertDatePickerValuesToString(dayOfMonth: Int, monthOfYear: Int, year: Int, format: String): String {
        val newDate = Calendar.getInstance()
        newDate.set(year, monthOfYear, dayOfMonth)
        val simpleDateFormat = SimpleDateFormat(format, Locale("tr"))
        return simpleDateFormat.format(newDate.time)
    }

    fun convertStringToFormattedString(date: String, format: String): String {
        val date1: Date
        date1 = stringToDate(date, "yyyy-MM-dd'T'HH:mm:ss")
        val formatter = SimpleDateFormat(format, Locale("tr"))

        return formatter.format(date1)
    }


    fun getFormattedSeconds(seconds: Long): String {
        val h = seconds.toInt() / 3600
        val m = seconds.toInt() / 60 % 60
        val s = seconds.toInt() % 60
        return String.format(Locale("tr"), "%d:%02d:%02d", h, m, s)
    }

    /**
     * String to Date
     *
     * @param dateString date string
     * @param format
     * @return date
     */
    fun stringToDate(dateString: String, format: String): Date {
        try {
            val df = SimpleDateFormat(format, Locale("tr"))
            df.timeZone = TimeZone.getTimeZone("Etc/GMT-0")
            return df.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
            return Date()
        }

    }

    //MS -> Miliseconds
    fun convertMSToFormattedString(format: String, millis: Long): String {
        val date = Date(millis)
        val formatter = SimpleDateFormat(format, Locale("tr"))
        return formatter.format(date)
    }

    fun stringToCalendar(dateString: String, format: String): Calendar? {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(format, Locale.ENGLISH)
        try {
            cal.time = sdf.parse(dateString)
            return cal
        } catch (e: ParseException) {
            e.printStackTrace()
            return null

        }

    }

    fun getDifferenceFromNow(date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale("tr"))
        try {
            val parse = sdf.parse(date)
            val currentTime = Calendar.getInstance().time
            val difference = parse.time - currentTime.time
            return difference / (60 * 1000)

        } catch (e: ParseException) {
            e.printStackTrace()
            return 0
        }

    }
}
