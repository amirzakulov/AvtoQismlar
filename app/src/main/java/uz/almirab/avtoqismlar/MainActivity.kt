package uz.almirab.avtoqismlar

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.almirab.avtoqismlar.api.ApiRequest
import uz.almirab.avtoqismlar.api.BASE_URL
import uz.almirab.avtoqismlar.databinding.ActivityMainBinding
import uz.almirab.avtoqismlar.fragments.*
import uz.almirab.mytestapp.util.LocaleHelper
import io.paperdb.Paper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
//    private lateinit var dollarRate: MutableList<String>

    //Fragment Navigation Bar
    private var selectedMenuItem = false
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Paper.init(this@MainActivity)

        val sharedPreference: SharedPreferences = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val access_token = sharedPreference.getString("access_token","").toString()



        binding.bottomNavigationBar.setSelectedItemId(R.id.menuHome)

        selectedMenuItem = intent.getBooleanExtra("selectedMenuItem", false)

        val appLang = sharedPreference.getString("appLang", "")

//        d("LANG TAG", "access_token: ${access_token}")

        val newLang = LocaleHelper().getLanguage(this@MainActivity)

//        d("LANG TAG", "appLang: ${appLang} -- newLang: ${newLang}")

        if(selectedMenuItem) {
            binding.bottomNavigationBar.selectedItemId = R.id.menuCart
            loadFragment(ShoppingCartFragment())
        } else if(appLang!!.isNotEmpty() && appLang != newLang){
            val editor = sharedPreference.edit()
            editor.putString("appLang", newLang)
            editor.commit()

            binding.bottomNavigationBar.selectedItemId = R.id.menuSettings
            loadFragment(SettingsFragment())
        } else {
            binding.bottomNavigationBar.selectedItemId = R.id.menuHome
            loadFragment(HomeFragment())
        }

        if(access_token.isEmpty()) {
            binding.bottomNavigationBar.visibility = View.GONE
        }
        //Fragment load
        binding.bottomNavigationBar.setOnItemSelectedListener {

            when(it.itemId) {
                R.id.menuHome -> loadFragment(HomeFragment())
                R.id.menuHistory -> loadFragment(HistoryFragment())
                R.id.menuCart -> loadFragment(ShoppingCartFragment())
                R.id.menuSettings -> loadFragment(SettingsFragment())

            }
            true
        }
    }

    // look at this section
    private fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container,fragment)
        if(supportFragmentManager.fragments.isNotEmpty()) { transaction.addToBackStack(null) }
        transaction.commit()
    }

    override fun attachBaseContext(base: Context) {
        val lang = LocaleHelper().getLanguage(base)
        LocaleHelper().setLocale(base, lang)
        super.attachBaseContext(LocaleHelper().onAttach(base, lang))
    }

    override fun onBackPressed() {
        if(binding.bottomNavigationBar.selectedItemId == R.id.menuHome) {
            if (backPressedTime + 3000 > System.currentTimeMillis()) {
                super.onBackPressed()
                finish()
            } else {
                Toast.makeText(this, "Dasturdan chiqish uchun yana bir bor bosing.", Toast.LENGTH_LONG).show()
            }
            backPressedTime = System.currentTimeMillis()
        } else {
            binding.bottomNavigationBar.selectedItemId = R.id.menuHome
        }
    }

}

