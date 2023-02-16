package uz.almirab.avtoqismlar

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import biz.gina.southernbreezetour.ui.view.notification.NotificationCountSetClass
import io.paperdb.Paper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.almirab.avtoqismlar.adapters.ProductsAdapter
import uz.almirab.avtoqismlar.api.ApiRequest
import uz.almirab.avtoqismlar.api.BASE_URL
import uz.almirab.avtoqismlar.databinding.ActivityProductsBinding
import uz.almirab.avtoqismlar.databinding.ProductLayoutBinding
import uz.almirab.avtoqismlar.fragments.HomeFragment
import uz.almirab.avtoqismlar.models.CartItem
import uz.almirab.avtoqismlar.models.Categories
import uz.almirab.avtoqismlar.models.Currency
import uz.almirab.avtoqismlar.models.ProductModel
import uz.almirab.avtoqismlar.util.DollarRate
import uz.almirab.avtoqismlar.util.MixFunctions
import uz.almirab.avtoqismlar.util.ShoppingCart
import uz.almirab.mytestapp.util.LocaleHelper
import java.io.Serializable
import java.util.*


class ProductsActivity : AppCompatActivity(), Serializable, ProductsAdapter.AddToCartOnItemClickListner {

    private lateinit var binding: ActivityProductsBinding
    private lateinit var category: Categories
    private lateinit var products: MutableList<ProductModel>
    private lateinit var tempProducts: MutableList<ProductModel>
    private var category_id: Int = 0
    private var category_name: String = ""
    private var access_token = ""
    private lateinit var selectedLang: String

    private lateinit var swipeRefresh: SwipeRefreshLayout

    companion object {
        val SELECTED_CATEGORY = "selected_category"
    }

    private lateinit var recyclerView: RecyclerView

    var notificationCountCart = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Paper.init(this@ProductsActivity)

        selectedLang = LocaleHelper().getLanguage(this@ProductsActivity)

        val sharedPreference: SharedPreferences = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        access_token = sharedPreference.getString("access_token","").toString()

        val actionBar: ActionBar? = supportActionBar
        actionBar!!.title = ""

        category        = intent.getSerializableExtra(SELECTED_CATEGORY) as Categories
        category_name   = category.name
        category_id     = category.id

        binding.categoryTitle.text = category_name
        tempProducts = mutableListOf<ProductModel>()

        val view = View.inflate(this@ProductsActivity, R.layout.waiting_white_dialog, null)
        val builder = AlertDialog.Builder(this@ProductsActivity)
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        this.makeApiRequest(category, dialog)

        swipeRefresh = binding.refreshProductsList
        binding.refreshProductsList.setOnRefreshListener {
            tempProducts.clear()
            this.makeApiRequest(category, dialog)
            swipeRefresh.isRefreshing = false

        }
    }

    private fun makeApiRequest(category: Categories, waitingDialog: AlertDialog) {
        val api = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
            GsonConverterFactory.create()).build()
            .create(ApiRequest::class.java)

        api.getProducts(category.id).enqueue(object: Callback<MutableList<ProductModel>> {
            override fun onResponse(call: Call<MutableList<ProductModel>>, response: Response<MutableList<ProductModel>>) {
                products = response.body()!!
                tempProducts.addAll(products)
                showProducts(tempProducts, category)

                waitingDialog.dismiss()
            }

            override fun onFailure(call: Call<MutableList<ProductModel>>, t: Throwable) {
                Log.d("Error", "onFailure: ${t}")

                waitingDialog.dismiss()
            }

        })
    }

    private fun showProducts(products: List<ProductModel>, category: Categories) {

//        d("SUCCESS", "Success: ${products}")
//        d("SUCCESS", "Size: "+products.size)

        if(products.size > 0) {
            binding.awaitingProducts.visibility = View.GONE
            binding.products.visibility = View.VISIBLE
            recyclerView                = binding.products
            recyclerView.layoutManager  = LinearLayoutManager(this@ProductsActivity)
            recyclerView.adapter        = ProductsAdapter(this@ProductsActivity, products, category,  this)
        } else {
            binding.awaitingProducts.visibility = View.VISIBLE
            binding.products.visibility = View.GONE
        }



    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.top_menu, menu)

        val item = menu.findItem(R.id.action_search)
        val searchView = item.actionView as SearchView
        searchView.setQueryHint(getString(R.string.product_search))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                tempProducts.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())
                if(searchText.isNotEmpty()) {
                    products.forEach {
                        val productName = if(selectedLang == "en") it.name else it.name_ru

                        if(productName.lowercase(Locale.getDefault()).contains(searchText)) {
                            tempProducts.add(it)
                        }
                    }
                } else {
                    tempProducts.clear()
                    tempProducts.addAll(products)
                }

                if(products.size > 0) {
                    recyclerView.adapter!!.notifyDataSetChanged()
                }

                return false
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // Get the notifications MenuItem and its LayerDrawable (layer-list)
        val item = menu.findItem(R.id.action_cart)
        if(ShoppingCart.getShoppingCartSize() > 0){
            notificationCountCart = ShoppingCart.getShoppingCartSize()
        }

        NotificationCountSetClass.setAddToCart(this@ProductsActivity, item, notificationCountCart)

        item.isVisible = access_token.isNotEmpty()

        invalidateOptionsMenu() // force the ActionBar to relayout its MenuItems. onCreateOptionsMenu(Menu) will be called again.

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_cart){
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("selectedMenuItem", true)
            startActivity(intent)

            return true;
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(position: Int, view: ProductLayoutBinding, product: ProductModel) {
        Toast.makeText(this, "Maxsulot savatga qo'shildi", Toast.LENGTH_LONG).show()
        product.category_id     = category_id
        product.category_name   = category_name

        val item = CartItem(product)
        ShoppingCart.addItem(item)

        notificationCountCart = ShoppingCart.getShoppingCartSize()

        NotificationCountSetClass.setNotifyCount(notificationCountCart)
        invalidateOptionsMenu()
    }

    override fun onBackPressed() {

        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

    }


}