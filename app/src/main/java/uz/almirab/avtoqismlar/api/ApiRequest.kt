package uz.almirab.avtoqismlar.api

import retrofit2.Call
import retrofit2.http.*
import uz.almirab.avtoqismlar.models.*

//const val BASE_URL = "http://192.168.43.107";
//const val BASE_URL = "http://192.168.2.248";
const val BASE_URL = "http://avtoqismlar.almirab.uz";

const val CATEGORY_IMAGE_URL = "/public/uploads/categories/";
const val PRODUCT_IMAGE_URL  = "/public/uploads/products/";
const val ADS_IMAGE_URL  = "/public/uploads/ads/";

interface ApiRequest {
    @GET("/api/get_categories")
    fun getCategories() : Call<List<Categories>>

    @GET("/api/get_products/{category_id}")
    fun getProducts(@Path("category_id")  category_id: Int, ) : Call<MutableList<ProductModel>>

    @GET("/api/get_product_similar/{product_id}/{category_id}")
    fun getProduct(
        @Path("product_id")  product_id: Int,
        @Path("category_id")  category_id: Int)
    : Call<SimilarProducts>

    @POST("/api/create_order")
    fun createOrder (
        @Body OrderDetails: OrderDetails,
    ): Call<Boolean>

    @GET("/api/client_orders/{client_id}")
    fun clientOrders (
        @Path("client_id")  client_id: Int,
    ): Call<List<OrderModel>>

//    @GET("https://cbu.uz/uz/arkhiv-kursov-valyut/json/")
//    fun currencyRate (): Call<List<Currency>>

    @POST("/api/register")
    fun createUser(
        @Body userData: UserData,
    ):Call<UserRegistrationData>

    @POST("/api/check_registration_code")
    fun verifyCode (
        @Body verificationCodeData: VerificationCodeData,
    ): Call<VerifiedUserData>

    @FormUrlEncoded
    @POST("/api/check_user_phone")
    fun checkUserPhone(
        @Field("phone") phone: String,
    ):Call<PhoneVerificationData>

    @FormUrlEncoded
    @POST("/api/login")
    fun login(
        @Field("phone") phone: String,
    ):Call<LoggedInData>

    @POST("/api/update_client")
    fun updateClientData (
        @Body userEditData: UserEditData,
    ): Call<Boolean>

//    @GET("/api/get_clients_count")
//    fun getClientsCount (
//    ): Call<Int>

    @GET("/api/get_ads")
    fun getAds (): Call<adsImages>


}