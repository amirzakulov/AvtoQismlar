package uz.almirab.avtoqismlar.adapters

import android.content.Context
import android.content.Intent
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uz.almirab.avtoqismlar.ProductActivity
import uz.almirab.avtoqismlar.R
import uz.almirab.avtoqismlar.api.BASE_URL
import uz.almirab.avtoqismlar.api.PRODUCT_IMAGE_URL
import uz.almirab.avtoqismlar.databinding.SimililarProductLayoutBinding
import uz.almirab.avtoqismlar.models.Categories
import uz.almirab.avtoqismlar.models.Currency
import uz.almirab.avtoqismlar.models.ProductModel
import uz.almirab.avtoqismlar.util.DollarRate
import uz.almirab.avtoqismlar.util.MixFunctions
import uz.almirab.mytestapp.util.LocaleHelper

class SimilarProductsAdapter(val context: Context?, private var products: List<ProductModel>?,val category: Categories) : RecyclerView.Adapter<SimilarProductsAdapter.ProductViewHolder>() {

//    private val dollarRate: Currency = DollarRate.getDollarData()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(SimililarProductLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products!![position]
        holder.setData(product, category.id, position)
    }

    override fun getItemCount() = products!!.size

    inner class ProductViewHolder(val binding: SimililarProductLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(product: ProductModel, category_id: Int, position: Int) {

            Glide.with(context!!).load(BASE_URL + PRODUCT_IMAGE_URL + product.picture)
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

            val productPriceUzb = MixFunctions().roundPriceUp(product.sell_price.toDouble() * product.dollar_rate.toDouble())

            binding.productPrice.text   = context.getString(R.string.sum_title, MixFunctions().numberFormat(productPriceUzb))

            binding.similarProduct.setOnClickListener {

                val intent = Intent(context, ProductActivity::class.java)
                intent.putExtra(ProductActivity.CATEGORY, category)
                intent.putExtra("product_id", product.id)
                context.startActivity(intent)

            }
        }
    }
}