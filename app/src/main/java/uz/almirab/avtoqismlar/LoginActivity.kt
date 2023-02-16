package uz.almirab.avtoqismlar

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.almirab.avtoqismlar.api.ApiRequest
import uz.almirab.avtoqismlar.api.BASE_URL
import uz.almirab.avtoqismlar.databinding.ActivityLoginBinding
import uz.almirab.avtoqismlar.models.*
import uz.almirab.mytestapp.util.LocaleHelper

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPhone: String
    private var timer: CountDownTimer? = null
    private var timerAttempts = 0
    private var timerWaitingSeconds:Long = 180000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
//
//            Log.d("TAG", token.toString())
//            Toast.makeText(baseContext, token.toString(), Toast.LENGTH_SHORT).show()
        })


        binding.tasdiqlashCodeKelmasa.text = getString(R.string.tasdiqlash_code_kelmasa, "3")
        binding.loginBtn.setOnClickListener { checkUserData() }
        binding.verifyCodeBtn.setOnClickListener {
            val verificationCode = binding.verificationCode.text.toString()

            if(verificationCode.isEmpty()) {
                binding.validationCodeInvalid.text = getString(R.string.validation_code_empty)
                binding.validationCodeInvalid.visibility = View.VISIBLE
            } else if(verificationCode.length < 6) {
                binding.validationCodeInvalid.text = getString(R.string.validation_code_invalid)
                binding.validationCodeInvalid.visibility = View.VISIBLE
            }else {
                binding.validationCodeInvalid.text = ""
                binding.validationCodeInvalid.visibility = View.INVISIBLE

                val device      = android.os.Build.DEVICE
                val brand       = android.os.Build.BRAND
                val model       = android.os.Build.MODEL
                val deviceName  = "$brand $model"
                val verificationCodeData = VerificationCodeData(phone = userPhone, code = verificationCode, deviceName = deviceName)
                sendVerificationCode(verificationCodeData)
            }

        }
    }

    private fun sendVerificationCode(verificationCodeData: VerificationCodeData) {

        binding.phoneProgressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
            GsonConverterFactory.create()).build()
        val api = retrofit.create(ApiRequest::class.java)
        api.verifyCode(verificationCodeData).enqueue(object : Callback<VerifiedUserData> {
            override fun onResponse(call: Call<VerifiedUserData>, response: Response<VerifiedUserData>) {

                val response = response.body()
//                Log.d("TAG Success", "${response}")
                val codeVerified = response!!.codeVerified

                if(!codeVerified) {
                    binding.validationCodeInvalid.text = getString(R.string.validation_code_invalid)
                    binding.validationCodeInvalid.visibility = View.VISIBLE
                } else {
                    binding.validationCodeInvalid.text = ""
                    binding.validationCodeInvalid.visibility = View.VISIBLE

                    val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
                    val editor = sharedPreference.edit()
                    editor.putInt("client_id",response.client_id)
                    editor.putString("fullName",response.fullName)
                    editor.putString("phone",response.phone)
                    editor.putString("address",response.address)
                    editor.putString("access_token",response.token)
                    editor.putString("token_type",response.token_type)
                    editor.putString("appLang", LocaleHelper().getLanguage(this@LoginActivity))
                    editor.putLong("l",100L)
                    editor.commit()

                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()

                }

            }

            override fun onFailure(call: Call<VerifiedUserData>, t: Throwable) {
                binding.phoneProgressBar.visibility = View.GONE
                Log.d("TAG Error", "${t}")
            }
        })
    }

    private fun checkUserData(){
        val phone = binding.userPhone.text.trim().toString()
        val phone_error_txt = binding.phoneError

        val phoneNumber = "+998" + phone
        val phoneValidation = phoneValidate(phone)

        if(phoneValidation.first) {
            phone_error_txt.text = phoneValidation.second
            phone_error_txt.visibility = View.VISIBLE

            binding.userPhone.requestFocus()
            return
        } else {
            phone_error_txt.text = ""
            phone_error_txt.visibility = View.GONE
        }

        binding.phoneProgressBar.visibility = View.VISIBLE
        binding.verificationProgressBar.visibility = View.GONE

        userPhone = phoneNumber
        checkUserCredential(phoneNumber)
    }

    private fun checkUserCredential(phoneNumber: String) {
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
            GsonConverterFactory.create()).build()
        val api = retrofit.create(ApiRequest::class.java)
        api.checkUserPhone(phoneNumber).enqueue(object : Callback<PhoneVerificationData> {
                override fun onResponse(call: Call<PhoneVerificationData>, response: Response<PhoneVerificationData>) {
                    val verificationData = response.body()!!
//                    Log.d("LOGIN TAG", response.toString())
                    binding.phoneProgressBar.visibility = View.GONE
                    if(!verificationData.isPhoneRegistered) {
                        val builder = AlertDialog.Builder(this@LoginActivity)
                        builder.setTitle("Xatolik!")
                        builder.setIcon(R.drawable.ic_error)
                        builder.setMessage(verificationData.errorMessage)
                        builder.setNegativeButton("Berkitish"){dialog, i ->
                            dialog.dismiss()
                        }
                        builder.show()
                    } else {
                        binding.loginText.visibility = View.INVISIBLE
                        binding.phoneNumberSendLayout.visibility = View.INVISIBLE
                        binding.verificationLayout.visibility = View.VISIBLE
                        showCodeVerificationBlock()
                    }

                }

                override fun onFailure(call: Call<PhoneVerificationData>, t: Throwable) {
                    Log.d("LOGIN_TAG_ERROR", t.toString())
                }
            })
    }

    fun phoneValidate(phone: String): Pair<Boolean, String>{
        var error = false
        var error_message = ""

        if(phone.isEmpty()) {
            error = true
            error_message = getString(R.string.phone_empty_error)
        } else if (phone.length != 9 || !Patterns.PHONE.matcher(phone).matches()) {
            error = true
            error_message = getString(R.string.phone_number_invalid_error)
        }

        return Pair(error, error_message)
    }

    private fun showCodeVerificationBlock() {
        timerAttempts++
        if(timerAttempts > 1) {
            binding.resendCodeTv.visibility = View.INVISIBLE
            timerWaitingSeconds = 1800000
            binding.tasdiqlashCodeKelmasa.text = getString(R.string.tasdiqlash_code_kelmasa, "30")
        }

        timer?.cancel()
        timer = object : CountDownTimer(timerWaitingSeconds, 1000){
            override fun onTick(secondsLeft: Long) {

                val minuts: Int = ((secondsLeft / 1000) / 60).toInt()
                val seconds: Int = ((secondsLeft / 1000) % 60).toInt()
                val timeLeft: String = makeTimeString(minuts, seconds)

                binding.timer.text = timeLeft
            }

            override fun onFinish() {
                binding.resendCodeTv.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun makeTimeString(minuts: Int, seconds: Int) : String = String.format("%02d:%02d", minuts, seconds)
}