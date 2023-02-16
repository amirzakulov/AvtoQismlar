package uz.almirab.avtoqismlar.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uz.almirab.avtoqismlar.R
import uz.almirab.avtoqismlar.api.BASE_URL
import uz.almirab.avtoqismlar.api.PRODUCT_IMAGE_URL
import uz.almirab.avtoqismlar.databinding.HistoryListChildBinding
import uz.almirab.avtoqismlar.models.Currency
import uz.almirab.avtoqismlar.models.ProductModel
import uz.almirab.avtoqismlar.util.DollarRate
import uz.almirab.avtoqismlar.util.MixFunctions
import uz.almirab.mytestapp.util.LocaleHelper

class HistoryProductsAdapter(val context : Context, private val products: List<ProductModel>)  : RecyclerView.Adapter<HistoryProductsAdapter.ProductViewHolder>() {

//    private val dollarRate: Currency = DollarRate.getDollarData()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(HistoryListChildBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.setData(product, position)
    }

    override fun getItemCount() = products.size

    inner class ProductViewHolder(val binding: HistoryListChildBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("StringFormatMatches")
        fun setData(product: ProductModel, position: Int) {

            Glide.with(context).load(BASE_URL + PRODUCT_IMAGE_URL +product.picture)
                .skipMemoryCache(false)
                .into(binding.productImage)

            val selectedLang = LocaleHelper().getLanguage(context)
            var product_name = product.name
            var product_country = product.country
            if(selectedLang == "uz") {
                product_name = product.name_ru
                product_country = product.country_ru
            }
            binding.productName.text = product_name
            binding.productBrand.text = product.brand
            binding.productCountry.text = product_country

//            binding.productName.text = product.name
            binding.productBrand.text = product.brand
//            binding.productCountry.text = product.country

            val dollarRate          = product.dollar_rate.toDouble()
            val productPriceUsd     = product.sell_price.toDouble()
            val productDiscountUsd  = product.discount.toDouble()
            val productPriceUzb     = MixFunctions().roundPriceUp(productPriceUsd * dollarRate)
            val productDiscountUzb  = MixFunctions().roundPriceUp(productDiscountUsd * dollarRate)
//            val productPriceUzb     = MixFunctions().roundPriceUp(productPriceUsd * dollarRate.Rate.toDouble())
//            val productDiscountUzb  = MixFunctions().roundPriceUp(productDiscountUsd * dollarRate.Rate.toDouble())
            if(productDiscountUsd == 0.0) {
                binding.productPrice.text               = context.getString(R.string.sum_title_history, product.count, MixFunctions().numberFormat(productPriceUzb))
                binding.productDiscountPrice.visibility = View.INVISIBLE
            } else {
                val productPrice = productPriceUzb - productDiscountUzb

                binding.productPrice.text           = context.getString(R.string.sum_title_history, product.count, MixFunctions().numberFormat(productPrice))
                binding.productDiscountPrice.text   = context.getString(R.string.sum_title, MixFunctions().numberFormat(productPriceUzb))
            }

            if(product.guarantee == 1) {
                binding.productGuarantee.visibility = View.VISIBLE
            }
        }
    }


}