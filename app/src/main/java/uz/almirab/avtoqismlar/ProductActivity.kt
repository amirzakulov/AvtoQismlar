package uz.almirab.avtoqismlar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.smarteist.autoimageslider.SliderView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.almirab.avtoqismlar.adapters.ProductSliderAdapter
import uz.almirab.avtoqismlar.adapters.ProductsAdapter
import uz.almirab.avtoqismlar.adapters.SimilarProductsAdapter
import uz.almirab.avtoqismlar.adapters.SliderAdapter
import uz.almirab.avtoqismlar.api.ApiRequest
import uz.almirab.avtoqismlar.api.BASE_URL
import uz.almirab.avtoqismlar.api.CATEGORY_IMAGE_URL
import uz.almirab.avtoqismlar.api.PRODUCT_IMAGE_URL
import uz.almirab.avtoqismlar.databinding.ActivityProductBinding
import uz.almirab.avtoqismlar.models.*
import uz.almirab.avtoqismlar.util.MixFunctions
import uz.almirab.mytestapp.util.LocaleHelper
import java.text.NumberFormat

class ProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var recyclerView: RecyclerView

//    private val dollarRate: Currency = DollarRate.getDollarData()

    //Slider
    lateinit var imageUrl: ArrayList<String>    //on below line we are creating a variable for our array list for storing our images.
    lateinit var sliderView: SliderView         //on below line we are creating a variable for our slider view.
    lateinit var imageSliderAdapter: ProductSliderAdapter   //on below line we are creating a variable for our slider adapter.
    private lateinit var category: Categories

    companion object {
        var CATEGORY = "category"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        category = intent.getSerializableExtra(CATEGORY) as Categories
        val productId:Int = intent.getIntExtra("product_id", 1)

//        d("TAG", category.toString())

        this.makeApiRequest(productId, category)

    }

    private fun makeApiRequest(product_id: Int, category: Categories) {
        val api = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
            GsonConverterFactory.create()).build()
            .create(ApiRequest::class.java)

        api.getProduct(product_id, category.id).enqueue(object: Callback<SimilarProducts> {
            override fun onResponse(call: Call<SimilarProducts>, response: Response<SimilarProducts>) {
                val response        = response.body()
                val product         = response!!.product
                val similarProducts = response.similarProducts
                val productImages   = response.productImages

                showProduct(product, productImages)

                if(similarProducts.size > 0) {
                    binding.simililarProductsLayout.visibility = View.VISIBLE
                } else {
                    binding.simililarProductsLayout.visibility = View.GONE
                }

                showSimilarProducts(similarProducts, category)
            }

            override fun onFailure(call: Call<SimilarProducts>, t: Throwable) {
                d("Error", "onFailure: ${t}")
            }

        })
    }

    private fun showProduct(product: ProductModel, productImages: List<ProductImage>) {

        d("product", "product: ${product}")

        val selectedLang = LocaleHelper().getLanguage(this)
        var product_name = product.name
        var product_country = product.country
        if(selectedLang == "uz") {
            product_name = product.name_ru
            product_country = product.country_ru
        }
        binding.productTitle.text = product_name
        binding.productBrand.text = product.brand
        binding.productCountry.text = product_country

        binding.productCategory.text = product.category_name
        binding.productDescription.text = product.description

        val dollarRate          = product.dollar_rate.toDouble()
        val productPriceUsd     = product.sell_price.toDouble()
        val productDiscountUsd  = product.discount.toDouble()
        val productPriceUzb     = MixFunctions().roundPriceUp(productPriceUsd * dollarRate)
        val productDiscountUzb  = MixFunctions().roundPriceUp(productDiscountUsd * dollarRate)
//        val productPriceUzb     = MixFunctions().roundPriceUp(productPriceUsd * dollarRate.Rate.toDouble())
//        val productDiscountUzb  = MixFunctions().roundPriceUp(productDiscountUsd * dollarRate.Rate.toDouble())
        if(productDiscountUsd == 0.0) {
            binding.productPrice.text               = getString(R.string.sum_title, MixFunctions().numberFormat(productPriceUzb))
            binding.productDiscountPrice.visibility = View.INVISIBLE
        } else {
            val productPrice = productPriceUzb - productDiscountUzb

            binding.productPrice.text           = getString(R.string.sum_title, MixFunctions().numberFormat(productPrice))
            binding.productDiscountPrice.text   = getString(R.string.sum_title, MixFunctions().numberFormat(productPriceUzb))
        }

        //Slider
        sliderView  = binding.productImagesSlider // on below line we are initializing our slier view.
        imageUrl    = ArrayList() // on below line we are initializing our image url array list.

        if(productImages.size > 0) {
            binding.noPicture.visibility = View.GONE
            binding.productImagesSlider.visibility = View.VISIBLE

            productImages.forEach {
                val picture = BASE_URL + PRODUCT_IMAGE_URL + it.picture
                imageUrl = (imageUrl + picture) as ArrayList<String>
            }

            imageSliderAdapter            = ProductSliderAdapter(imageUrl) //on below line we are initializing our slider adapter and adding our list to it.
            sliderView.autoCycleDirection   = SliderView.LAYOUT_DIRECTION_LTR // on below line we are setting auto cycle direction for our slider view from left to right.

            sliderView.setSliderAdapter(imageSliderAdapter) // on below line we are setting adapter for our slider.

            sliderView.scrollTimeInSec      = 3 // on below line we are setting scroll time in seconds for our slider view.
            sliderView.isAutoCycle          = true // on below line we are setting auto cycle to true to auto slide our items.

            // on below line we are calling start auto cycle to start our cycle.
            sliderView.startAutoCycle()
        } else {

            if(!product.picture.isNullOrEmpty()) {

                d("PICTURE", BASE_URL + PRODUCT_IMAGE_URL + product.picture)

                Glide.with(this@ProductActivity).load(BASE_URL + PRODUCT_IMAGE_URL +product.picture)
                    .skipMemoryCache(false)
                    .into(binding.noPicture)
            } else {
                binding.noPicture.setImageDrawable(this@ProductActivity.getDrawable(R.drawable.no_picture))
            }

            binding.noPicture.visibility = View.VISIBLE
            binding.productImagesSlider.visibility = View.GONE
        }

    }

    private fun showSimilarProducts(similarProducts: List<ProductModel>, category: Categories){
        recyclerView                = binding.simililarProducts
        recyclerView.layoutManager  = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter        = SimilarProductsAdapter(this@ProductActivity, similarProducts,  category)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ProductsActivity::class.java)
        intent.putExtra(ProductsActivity.SELECTED_CATEGORY, category)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}