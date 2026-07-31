package com.mnfarzaneh.solalrchef.util

private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun String.toPersianDigits(): String {
    val builder = StringBuilder(this.length)
    for (ch in this) {
        if (ch in '0'..'9') {
            builder.append(persianDigits[ch - '0'])
        } else {
            builder.append(ch)
        }
    }
    return builder.toString()
}

fun Int.toPersianDigits(): String = this.toString().toPersianDigits()
fun Float.toPersianDigits(): String = this.toString().toPersianDigits()