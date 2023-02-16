package uz.almirab.avtoqismlar.fragments

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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.almirab.avtoqismlar.MainActivity
import uz.almirab.avtoqismlar.R
import uz.almirab.avtoqismlar.api.ApiRequest
import uz.almirab.avtoqismlar.api.BASE_URL
import uz.almirab.avtoqismlar.databinding.FragmentSettingsBinding
import uz.almirab.avtoqismlar.models.UserEditData
import uz.almirab.mytestapp.util.LocaleHelper

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editPhone.visibility = View.GONE
        binding.savePhoneLayout.visibility = View.GONE

        binding.editAddress.visibility = View.GONE
        binding.saveAddressLayout.visibility = View.GONE

        val sharedPreferences = this.requireActivity().getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val clientId = sharedPreferences.getInt("client_id",0)
        val fullName = sharedPreferences.getString("fullName","").toString()
        val phone = sharedPreferences.getString("phone","").toString()
        val address = sharedPreferences.getString("address","").toString()

        binding.fullName.text   = fullName
        binding.phone.text      = phone
        binding.address.text    = address

        binding.editPhone.setText(phone)
        binding.editAddress.setText(address)

        binding.editPhoneBtn.setOnClickListener {

            binding.editPhone.visibility = View.VISIBLE
            binding.savePhoneLayout.visibility = View.VISIBLE
            binding.phone.visibility = View.GONE
            binding.editPhoneBtn.visibility = View.GONE
        }

        binding.cancelPhoneBtn.setOnClickListener {
            binding.editPhone.visibility = View.GONE
            binding.savePhoneLayout.visibility = View.GONE
            binding.phone.visibility = View.VISIBLE
            binding.editPhoneBtn.visibility = View.VISIBLE
        }

        binding.savePhoneBtn.setOnClickListener {

            val newPhoneNumber = binding.editPhone.text.toString().replace("\\s".toRegex(), "")
            val clientEditData = UserEditData(client_id = clientId, userField = "phone", userDetail = newPhoneNumber)
            makeApiRequest(clientEditData)

            editor.putString("phone", newPhoneNumber)
            editor.commit()
            binding.phone.text = newPhoneNumber
            binding.editPhone.setText(newPhoneNumber)

            binding.editPhone.visibility = View.GONE
            binding.savePhoneLayout.visibility = View.GONE
            binding.phone.visibility = View.VISIBLE
            binding.editPhoneBtn.visibility = View.VISIBLE
        }

        /* ************** Aress ***************** */
        binding.editAddressBtn.setOnClickListener {

            binding.editAddress.visibility          = View.VISIBLE
            binding.saveAddressLayout.visibility    = View.VISIBLE
            binding.address.visibility              = View.GONE
            binding.editAddressBtn.visibility       = View.GONE
        }

        binding.cancelAddressBtn.setOnClickListener {
            binding.editAddress.visibility = View.GONE
            binding.saveAddressLayout.visibility = View.GONE
            binding.address.visibility = View.VISIBLE
            binding.editAddressBtn.visibility = View.VISIBLE
        }

        binding.saveAddressBtn.setOnClickListener {

            val newAddress = binding.editAddress.text.toString()
            val clientEditData = UserEditData(client_id = clientId, userField = "address", userDetail = newAddress)
            makeApiRequest(clientEditData)

            editor.putString("address", newAddress)
            editor.commit()
            binding.address.text = newAddress
            binding.editAddress.setText(newAddress)

            binding.editAddress.visibility          = View.GONE
            binding.saveAddressLayout.visibility    = View.GONE
            binding.address.visibility              = View.VISIBLE
            binding.editAddressBtn.visibility       = View.VISIBLE
        }


        val lang = LocaleHelper().getLanguage(context)
        if(lang == "uz") binding.langCyrill.isChecked = true
        if(lang == "en") binding.langLatin.isChecked  = true

        binding.langLatin.setOnClickListener {
            LocaleHelper().setLocale(context, "en")
//            activity?.let { it -> ActivityCompat.recreate(it) }

            val intent = Intent(it.context, MainActivity::class.java)
            startActivity(intent)

        }

        binding.langCyrill.setOnClickListener {
            LocaleHelper().setLocale(context, "uz")
//            activity?.let { it -> ActivityCompat.recreate(it) }

            val intent = Intent(it.context, MainActivity::class.java)
            startActivity(intent)

        }

        binding.logout.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            val intent = Intent(it.context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun makeApiRequest(userEditData:UserEditData) {
        val api = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
            GsonConverterFactory.create()).build()
            .create(ApiRequest::class.java)

        api.updateClientData(userEditData).enqueue(object: Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                val response        = response.body()!!

                if(response) {
                    Toast.makeText(context, "Ma'lumotlar saqlandi", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Log.d("Error", "onFailure: ${t}")
            }

        })
    }

    //to avoid memory leaks
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}