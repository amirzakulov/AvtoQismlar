package uz.almirab.avtoqismlar.util

import android.content.Context

import io.paperdb.Paper
import uz.almirab.avtoqismlar.models.Currency

class DollarRate {
    companion object {

        fun addCurrency(currencyItem: Currency) {
            val currency = this.getCurrency()

            val targetItem = currency.singleOrNull { it.id == currencyItem.id }
            if (targetItem == null) {
                currency.add(currencyItem)
            } else if (targetItem.Date < currencyItem.Date) {
                targetItem.Rate = currencyItem.Rate
                targetItem.Date = currencyItem.Date
            }
            this.saveCart(currency)
        }

        fun saveCart(currency: MutableList<Currency>) {
            Paper.book().write("currency", currency)
        }

        fun getCurrency(): MutableList<Currency> {
            return Paper.book().read("currency", mutableListOf())!!
        }

        fun getDollarData(): Currency {
            val currency = this.getCurrency()

            val targetItem = currency.singleOrNull { it.Code == "840"}

            return targetItem!!
        }

        fun removeAll() {
            Paper.book().delete("cart");
            Paper.book().delete("currency");
        }

//        fun changeCurrencyRate(currencyItem: Currency) {
//            val currency = this.getCurrency()
//
//            val targetItem = currency.singleOrNull { it.id == currencyItem.id }
//            targetItem!!.Rate = currencyItem.Rate
//            targetItem.Date = currencyItem.Date
//
//            this.saveCart(currency)
//        }
    }
}