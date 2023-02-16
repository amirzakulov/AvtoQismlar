package uz.almirab.avtoqismlar.util

import java.text.NumberFormat
import kotlin.math.roundToInt

class MixFunctions {

    fun roundPriceUp(number: Double): Int {
        var n = (number/100)
        n = Math.ceil(n).toDouble()
        n = (n * 100)

        return n.roundToInt()
    }

    fun numberFormat(number: Int): String {
        return (String.format("%,d", number)).replace(',', ' ')
    }

}