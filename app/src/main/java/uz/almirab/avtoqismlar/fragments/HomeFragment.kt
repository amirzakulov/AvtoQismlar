package uz.almirab.avtoqismlar.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log.d
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smarteist.autoimageslider.SliderView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.almirab.avtoqismlar.LoginActivity
import uz.almirab.avtoqismlar.MainActivity
import uz.almirab.avtoqismlar.R
import uz.almirab.avtoqismlar.RegistrationActivity
import uz.almirab.avtoqismlar.adapters.CategoryAdapter
import uz.almirab.avtoqismlar.adapters.SliderAdapter
import uz.almirab.avtoqismlar.api.ADS_IMAGE_URL
import uz.almirab.avtoqismlar.api.ApiRequest
import uz.almirab.avtoqismlar.api.BASE_URL
import uz.almirab.avtoqismlar.api.PRODUCT_IMAGE_URL
import uz.almirab.avtoqismlar.databinding.FragmentHomeBinding
import uz.almirab.avtoqismlar.models.Categories
import uz.almirab.avtoqismlar.models.adsImages

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    //Slider
    lateinit var imageUrl: ArrayList<String> // creating a variable for our array list for storing our images.
    lateinit var sliderView: SliderView // creating a variable for our slider view.
    lateinit var sliderAdapter: SliderAdapter    // creating a variable for our slider adapter.

    //RecyclerView
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private var backPressedTime: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true);

        val sharedPreference: SharedPreferences = this.requireActivity().getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val access_token = sharedPreference.getString("access_token","").toString()

//        Toast.makeText(context, "Logging Details: ${access_token}", Toast.LENGTH_SHORT).show()


        val view = View.inflate(context, R.layout.waiting_white_dialog, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        makeApiRequestClientsCount()
        makeApiRequest(dialog)

        if(access_token.isNotEmpty()) {
            binding.sendToLogin.visibility      = View.GONE
            binding.sendToRegister.visibility   = View.GONE
        }

        binding.sendToRegister.setOnClickListener {
            val intent = Intent(context, RegistrationActivity::class.java)
            startActivity(intent)
        }

        binding.sendToLogin.setOnClickListener {
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    private fun makeApiRequestClientsCount() {
        val api = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
            GsonConverterFactory.create()).build()
            .create(ApiRequest::class.java)

        api.getAds().enqueue(object: Callback<adsImages>{
            override fun onResponse(call: Call<adsImages>, response: Response<adsImages>) {
                val res = response.body()!!
                val adsPictures = res.pictures
                val clientsCount = res.clientsCount

//                d("TAG ADS", "onResponse: $res")

                showAds(adsPictures)
                
                binding.clientsCount.text = clientsCount.toString()

            }

            override fun onFailure(call: Call<adsImages>, t: Throwable) {
                d("Error ADS", "onFailure: ${t}")
            }

        })
    }

    private fun showAds(adsPictures: List<String>) {
        //Slider
        // on below line we are initializing our slier view.
        sliderView = binding.slider

        // on below line we are initializing our image url array list.
        imageUrl = ArrayList()

        adsPictures.forEach { image ->
            val picture = BASE_URL + ADS_IMAGE_URL + image
            imageUrl = (imageUrl + picture) as ArrayList<String>
        }

//        imageUrl = (imageUrl + "https://practice.geeksforgeeks.org/_next/image?url=https%3A%2F%2Fmedia.geeksforgeeks.org%2Fimg-practice%2Fbanner%2Fdsa-self-paced-thumbnail.png&w=1920&q=75") as ArrayList<String>
//        imageUrl = (imageUrl + "https://practice.geeksforgeeks.org/_next/image?url=https%3A%2F%2Fmedia.geeksforgeeks.org%2Fimg-practice%2Fbanner%2Fdata-science-live-thumbnail.png&w=1920&q=75") as ArrayList<String>
//        imageUrl = (imageUrl + "https://practice.geeksforgeeks.org/_next/image?url=https%3A%2F%2Fmedia.geeksforgeeks.org%2Fimg-practice%2Fbanner%2Ffull-stack-node-thumbnail.png&w=1920&q=75") as ArrayList<String>

        sliderAdapter = SliderAdapter( imageUrl) // on below line we are initializing our slider adapter and adding our list to it.
        sliderView.autoCycleDirection = SliderView.LAYOUT_DIRECTION_LTR // on below line we are setting auto cycle direction for our slider view from left to right.
        sliderView.setSliderAdapter(sliderAdapter) // on below line we are setting adapter for our slider.
        sliderView.scrollTimeInSec = 5 // on below line we are setting scroll time in seconds for our slider view.
        sliderView.isAutoCycle = true         // on below line we are setting auto cycle to true to auto slide our items.
        sliderView.startAutoCycle()        // on below line we are calling start auto cycle to start our cycle.

    }

    private fun makeApiRequest(waitingDialog: AlertDialog) {
        val api = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
            GsonConverterFactory.create()).build()
            .create(ApiRequest::class.java)

        api.getCategories().enqueue(object: Callback<List<Categories>>{
            override fun onResponse(call: Call<List<Categories>>, response: Response<List<Categories>>) {
                val categoryList = response.body()

                recyclerView                = binding.carCategoriesList
                recyclerView.layoutManager  = GridLayoutManager(context, 2)
                categoryAdapter             = CategoryAdapter(context, categoryList)
                recyclerView.adapter        = categoryAdapter

                waitingDialog.dismiss()

            }

            override fun onFailure(call: Call<List<Categories>>, t: Throwable) {
                d("Error", "onFailure: ${t}")
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

}