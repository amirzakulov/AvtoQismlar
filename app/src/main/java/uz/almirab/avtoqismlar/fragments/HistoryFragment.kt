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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.almirab.avtoqismlar.MainActivity
import uz.almirab.avtoqismlar.R
import uz.almirab.avtoqismlar.adapters.HistoryAdapter
import uz.almirab.avtoqismlar.api.ApiRequest
import uz.almirab.avtoqismlar.api.BASE_URL
import uz.almirab.avtoqismlar.databinding.FragmentHistoryBinding
import uz.almirab.avtoqismlar.models.OrderModel
import uz.almirab.avtoqismlar.models.ProductModel

class HistoryFragment : Fragment(R.layout.fragment_history) {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    //RecyclerView
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreference: SharedPreferences = this.requireActivity().getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val clientId = sharedPreference.getInt("client_id",0)

        val view = View.inflate(context, R.layout.waiting_white_dialog, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        this.makeApiRequest(clientId, dialog)
    }


    private fun makeApiRequest(client_id: Int, waitingDialog: AlertDialog) {
        val api = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
            GsonConverterFactory.create()).build()
            .create(ApiRequest::class.java)

        api.clientOrders(client_id).enqueue(object: Callback<List<OrderModel>> {
            override fun onResponse(call: Call<List<OrderModel>>, response: Response<List<OrderModel>>) {
                val orders = response.body()
                showOrders(orders!!)

                if(orders.size > 0) {
                    binding.productsHistory.visibility = View.VISIBLE
                    binding.historyIsEmpty.visibility = View.GONE
                } else {
                    binding.productsHistory.visibility = View.GONE
                    binding.historyIsEmpty.visibility = View.VISIBLE
                }

                waitingDialog.dismiss()

//                d("Response", "ORDERS: ${orders}")
            }

            override fun onFailure(call: Call<List<OrderModel>>, t: Throwable) {
                waitingDialog.dismiss()
                d("Error", "onFailure: ${t}")

                Toast.makeText(context,"Serverda Xatolik, iltimos internetingizni tekshirib ko'ring...", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showOrders(orders: List<OrderModel>) {
        recyclerView                = binding.productsHistory
        recyclerView.layoutManager  = LinearLayoutManager(context)
        historyAdapter              = HistoryAdapter(context, orders)
        recyclerView.adapter        = historyAdapter
    }

    //to avoid momory leaks
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}