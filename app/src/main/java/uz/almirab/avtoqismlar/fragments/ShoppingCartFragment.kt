package uz.almirab.avtoqismlar.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.paperdb.Paper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.almirab.avtoqismlar.MainActivity
import uz.almirab.avtoqismlar.R
import uz.almirab.avtoqismlar.adapters.ShoppingCartAdapter
import uz.almirab.avtoqismlar.api.ApiRequest
import uz.almirab.avtoqismlar.api.BASE_URL
import uz.almirab.avtoqismlar.databinding.FragmentShoppingCartBinding
import uz.almirab.avtoqismlar.databinding.ShoppingCartListBinding
import uz.almirab.avtoqismlar.models.Currency
import uz.almirab.avtoqismlar.models.ProductModel
import uz.almirab.avtoqismlar.models.OrderDetails
import uz.almirab.avtoqismlar.util.DollarRate
import uz.almirab.avtoqismlar.util.MixFunctions
import uz.almirab.avtoqismlar.util.ShoppingCart


class ShoppingCartFragment : Fragment(R.layout.fragment_shopping_cart),
    ShoppingCartAdapter.RemoveProductOnItemClickListner, ShoppingCartAdapter.ProductCountChangeListner
{
    private var _binding: FragmentShoppingCartBinding? = null
    private val binding get() = _binding!!

    //RecyclerView
    private lateinit var recyclerView: RecyclerView
    private var productsList = mutableListOf<ProductModel>()
    private var orderList = mutableListOf<ProductModel>()
//    private val dollarRate: Currency = DollarRate.getDollarData()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    var total:Int = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val callback = object : OnBackPressedCallback(true){
//            override fun handleOnBackPressed() {
//                Toast.makeText(context, "Back Button Pressed", Toast.LENGTH_LONG).show()
//                val intent = Intent(context, MainActivity::class.java)
//                startActivity(intent)
//            }
//        }
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        Paper.init(requireContext())

        val sharedPreference: SharedPreferences = requireContext().getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)

        val shoppingCartSize = ShoppingCart.getShoppingCartSize()
        if(shoppingCartSize > 0) {
            ShoppingCart.getCart().forEach {

                productsList.add(it.product)
                val productPriceUzb = MixFunctions().roundPriceUp(it.product.sell_price.toDouble() * it.product.dollar_rate.toDouble())
                val productDiscountUzb = MixFunctions().roundPriceUp(it.product.discount.toDouble() * it.product.dollar_rate.toDouble())
//                val productPriceUzb = MixFunctions().roundPriceUp(it.product.sell_price.toDouble() * dollarRate.Rate.toDouble())
//                val productDiscountUzb = MixFunctions().roundPriceUp(it.product.discount.toDouble() * dollarRate.Rate.toDouble())
                total += MixFunctions().roundPriceUp(( productPriceUzb - productDiscountUzb) * it.product.count.toDouble())
            }

            binding.selectedProducts.isVisible = true
            binding.cartFooter.isVisible = true
            binding.cartIsEmpty.isVisible = false
        } else {
            binding.selectedProducts.isVisible = false
            binding.cartFooter.isVisible = false
            binding.cartIsEmpty.isVisible = true

        }

        binding.productsTotalPrice.text = getString(R.string.sum_title, MixFunctions().numberFormat(total))

        recyclerView                = binding.productsCart
        recyclerView.layoutManager  = LinearLayoutManager(context)
        recyclerView.adapter        = ShoppingCartAdapter(context, productsList,  this, this)


        binding.sendOrder.setOnClickListener {
            val clientId = sharedPreference.getInt("client_id",0).toString()
            ShoppingCart.getCart().forEach {
                orderList.add(it.product)
            }
//            val dollarRate = dollarRate.Rate.toDouble()
            val orderDetails = OrderDetails(client_id = clientId.toInt(), products = orderList)

            val view = View.inflate(context, R.layout.waiting_dialog, null)
            val builder = AlertDialog.Builder(context)
            builder.setView(view)
            val dialog = builder.create()
            dialog.setCancelable(false)
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            this.makeApiRequest(orderDetails, dialog)

//            d("OrderDetails:", orderDetails.toString())
        }
    }

    private fun makeApiRequest(orderDetails: OrderDetails, waitingDialog: AlertDialog) {
        val api = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
            GsonConverterFactory.create()).build()
            .create(ApiRequest::class.java)

        api.createOrder(orderDetails).enqueue(object: Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                val products = response.body()
//                d("Response", "onResponse: ${ShoppingCart.getCart()}")

                waitingDialog.dismiss()

                val view = View.inflate(context, R.layout.confirmation_dialog, null)
                val builder = AlertDialog.Builder(context)
                builder.setView(view)
                val dialog = builder.create()
                dialog.setCancelable(false)
                dialog.show()
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                val closeBtn = dialog.findViewById<Button>(R.id.close_payment_confirmation_dialog)
                closeBtn.setOnClickListener { it ->
                    ShoppingCart.removeAll()
                    val  intent = Intent(it.context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Log.d("Error", "onFailure: ${t}")
                waitingDialog.dismiss()

                Toast.makeText(context,"Serverda Xatolik, iltimos internetingizni tekshirib ko'ring...", Toast.LENGTH_LONG).show()
            }

        })
    }

    //to avoid momory leaks
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClick(position: Int, view: ShoppingCartListBinding, product: ProductModel, cartIsEmpty: Boolean) {

        total -= MixFunctions().roundPriceUp(product.sell_price.toDouble() * product.dollar_rate.toDouble())  * product.count
//        total -= MixFunctions().roundPriceUp(product.sell_price.toDouble() * dollarRate.Rate.toDouble())  * product.count

        binding.productsTotalPrice.text = getString(R.string.sum_title, MixFunctions().numberFormat(total))
        if(cartIsEmpty) {
            binding.selectedProducts.isVisible = false
            binding.cartFooter.isVisible = false
            binding.cartIsEmpty.isVisible = true
        }
    }

    override fun onCountChange(position: Int, view: ShoppingCartListBinding, products: MutableList<ProductModel>?) {

//        d("OnChangeTag", products.toString())
        total = 0
        products?.forEach {
//            total += MixFunctions().roundPriceUp((it.sell_price.toDouble() - it.discount.toDouble()) * dollarRate.Rate.toDouble()) * it.count
            total += MixFunctions().roundPriceUp((it.sell_price.toDouble() - it.discount.toDouble()) * it.dollar_rate.toDouble()) * it.count
        }

        binding.productsTotalPrice.text = getString(R.string.sum_title, MixFunctions().numberFormat(total))
    }
}