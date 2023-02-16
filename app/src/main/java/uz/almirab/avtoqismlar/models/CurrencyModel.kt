package uz.almirab.avtoqismlar.models

import java.io.Serializable

data class Currency(
    val id: Int,
    val Code: String,
    val Ccy: String,
    var Rate: String,
    var Date: String,
): Serializable
