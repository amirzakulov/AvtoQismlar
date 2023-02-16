package uz.almirab.avtoqismlar.adapters

import android.content.Context
import android.content.Intent
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.travijuu.numberpicker.library.NumberPicker
import uz.almirab.avtoqismlar.ProductActivity
import uz.almirab.avtoqismlar.R
import uz.almirab.avtoqismlar.api.BASE_URL
import uz.almirab.avtoqismlar.api.PRODUCT_IMAGE_URL
import uz.almirab.avtoqismlar.databinding.NumberPickerLayoutBinding
import uz.almirab.avtoqismlar.databinding.ShoppingCartListBinding
import uz.almirab.avtoqismlar.models.CartItem
import uz.almirab.avtoqismlar.models.Currency
import uz.almirab.avtoqismlar.models.ProductModel
import uz.almirab.avtoqismlar.util.DollarRate
import uz.almirab.avtoqismlar.util.MixFunctions
import uz.almirab.avtoqismlar.util.ShoppingCart
import uz.almirab.mytestapp.util.LocaleHelper

class ShoppingCartAdapter(val context: Context?, private var products: List<ProductModel>?,
                          private val removeProductOnItemClickListner: RemoveProductOnItemClickListner,
                          private val productCountChangeListner: ProductCountChangeListner
                          ) : RecyclerView.Adapter<ShoppingCartAdapter.ProductViewHolder>() {

    private var myProducts: MutableList<ProductModel>? = products as MutableList<ProductModel>?
//    private val dollarRate: Currency = DollarRate.getDollarData()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(ShoppingCartListBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            removeProductOnItemClickListner, productCountChangeListner)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = myProducts!![position]
        holder.setData(product, position)
    }

    override fun getItemCount() = myProducts!!.size

    inner class ProductViewHolder(val binding: ShoppingCartListBinding,
                                  private val removeProductOnItemClickListner: RemoveProductOnItemClickListner,
                                  private val productCountChangeListner: ProductCountChangeListner
                                  ) : RecyclerView.ViewHolder(binding.root) {
        fun setData(product: ProductModel, position: Int) {

//            d("PRODUCT", product.toString())

//            binding.productImage.setImageDrawable(context!!.getDrawable(product.picture))

            Glide.with(context!!).load(BASE_URL + PRODUCT_IMAGE_URL +product.picture)
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
//            binding.productBrand.text = product.brand
//            binding.productCountry.text = product.country
            binding.productCategory.text = product.category_name

            val dollarRate          = product.dollar_rate.toDouble()
            val productPriceUsd     = product.sell_price.toDouble()
            val productDiscountUsd  = product.discount.toDouble()
            val productPriceUzb     = MixFunctions().roundPriceUp(productPriceUsd * dollarRate)
            val productDiscountUzb  = MixFunctions().roundPriceUp(productDiscountUsd * dollarRate)
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

            binding.numberPicker.value = product.count

            binding.removeCartItem.setOnClickListener {

                var cartIsEmpty = false
                val item = CartItem(product)
                ShoppingCart.removeItem(item, context)

                deleteItem(position)

                Toast.makeText(itemView.context, "Maxsulot Savatdan o'chirildi!",Toast.LENGTH_LONG).show()

                if(ShoppingCart.getShoppingCartSize() == 0) cartIsEmpty = true
                removeProductOnItemClickListner.onClick(position, binding, product, cartIsEmpty)
            }

            binding.numberPicker.setValueChangedListener { value, action ->
                product.count = binding.numberPicker.value

//                Toast.makeText(context, value.toString(), Toast.LENGTH_SHORT).show()

                val item = CartItem(product)
                ShoppingCart.changeItemCount(item)

                productCountChangeListner.onCountChange(position, binding, myProducts)
            }
        }
    }

    //this is my custom interface for onClick event on item
    interface RemoveProductOnItemClickListner{
        fun onClick(position: Int, view: ShoppingCartListBinding, product: ProductModel, cartIsEmpty: Boolean)
    }

    interface ProductCountChangeListner{
        fun onCountChange(position: Int, view: ShoppingCartListBinding, products: MutableList<ProductModel>?)
    }

    private fun deleteItem(position: Int){
        myProducts?.removeAt(position)
        notifyDataSetChanged()
    }

}