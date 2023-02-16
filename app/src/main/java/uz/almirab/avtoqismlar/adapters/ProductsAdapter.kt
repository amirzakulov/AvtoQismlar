package uz.almirab.avtoqismlar.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uz.almirab.avtoqismlar.ProductActivity
import uz.almirab.avtoqismlar.ProductsActivity
import uz.almirab.avtoqismlar.R
import uz.almirab.avtoqismlar.api.BASE_URL
import uz.almirab.avtoqismlar.api.PRODUCT_IMAGE_URL
import uz.almirab.avtoqismlar.databinding.ProductLayoutBinding
import uz.almirab.avtoqismlar.models.Categories
import uz.almirab.avtoqismlar.models.Currency
import uz.almirab.avtoqismlar.models.ProductModel
import uz.almirab.avtoqismlar.util.DollarRate
import uz.almirab.avtoqismlar.util.MixFunctions
import uz.almirab.mytestapp.util.LocaleHelper
import kotlin.math.roundToInt

class ProductsAdapter(val context: Context?, private var products: List<ProductModel>?, val category: Categories, private val addToCartOnItemClickListner: AddToCartOnItemClickListner) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

//    private val dollarRate: Currency = DollarRate.getDollarData()
    val sharedPreference: SharedPreferences = context!!.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
    val access_token = sharedPreference.getString("access_token","").toString()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(ProductLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false), addToCartOnItemClickListner)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products!![position]
        holder.setData(product, category.id, position)
    }

    override fun getItemCount() = products!!.size

    inner class ProductViewHolder(val binding: ProductLayoutBinding, private val addToCartOnItemClickListner: AddToCartOnItemClickListner) : RecyclerView.ViewHolder(binding.root) {
        fun setData(product: ProductModel, category_id:Int, position: Int) {

            if(!product.picture.isNullOrEmpty()) {
                Glide.with(context!!).load(BASE_URL + PRODUCT_IMAGE_URL +product.picture)
                    .skipMemoryCache(false)
                    .into(binding.productImage)
            } else {
                binding.productImage.setImageDrawable(context!!.getDrawable(R.drawable.no_picture))
            }

            if(access_token.isEmpty()) {
                binding.addToCart.visibility = View.GONE
                binding.numberPicker.visibility = View.GONE
            }

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


            val dollarRate          = product.dollar_rate.toDouble()
            val productPriceUsd     = product.sell_price.toDouble()
            val productDiscountUsd  = product.discount.toDouble()
            val productPriceUzb     = MixFunctions().roundPriceUp(productPriceUsd * dollarRate)
            val productDiscountUzb  = MixFunctions().roundPriceUp(productDiscountUsd * dollarRate)
//            val productPriceUzb     = MixFunctions().roundPriceUp(productPriceUsd * dollarRate.Rate.toDouble())
//            val productDiscountUzb  = MixFunctions().roundPriceUp(productDiscountUsd * dollarRate.Rate.toDouble())
            if(productDiscountUsd == 0.0) {
                binding.productPrice.text               = context.getString(R.string.sum_title, MixFunctions().numberFormat(productPriceUzb))
                binding.productDiscountPrice.visibility = View.INVISIBLE
            } else {
                val productPrice = productPriceUzb - productDiscountUzb

                binding.productPrice.text           = context.getString(R.string.sum_title, MixFunctions().numberFormat(productPrice))
                binding.productDiscountPrice.text   = context.getString(R.string.sum_title, MixFunctions().numberFormat(productPriceUzb))
            }

            if(product.guarantee == 1) {
                binding.productGuarantee.visibility = View.VISIBLE
            }

            binding.productName.setOnClickListener {
                val intent = Intent(context, ProductActivity::class.java)
                intent.putExtra(ProductActivity.CATEGORY, category)
                intent.putExtra("product_id", product.id)
                context.startActivity(intent)
//                Toast.makeText(itemView.context, "Product",Toast.LENGTH_LONG).show()
            }

            binding.productImage.setOnClickListener {
                val intent = Intent(context, ProductActivity::class.java)
                intent.putExtra("category", category)
                intent.putExtra("product_id", product.id)
                context.startActivity(intent)
            }

            binding.addToCart.setOnClickListener {
                val unitCount = binding.numberPicker.value
                product.count = unitCount
                addToCartOnItemClickListner.onClick(position, binding, product)
            }
        }
    }


    //this is my custom interface for onClick event on item
    interface AddToCartOnItemClickListner{
        fun onClick(position: Int, view: ProductLayoutBinding, product: ProductModel)
    }
}