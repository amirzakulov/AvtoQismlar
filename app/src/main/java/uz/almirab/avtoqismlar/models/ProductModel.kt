package uz.almirab.avtoqismlar.models

import java.io.Serializable

data class Categories (
    val id: Int,
    val name: String,
    val sort: Int,
    val picture: String,
    val active: Int

) : Serializable

data class ProductModel (
    val id: Int,
    val name: String,
    val name_ru: String,
    val brand: String,
    val country: String,
    val country_ru: String,
    val guarantee: Int,
    val picture: String,
    val sell_price: String,
    val price: String,
    val discount: String,
    var count: Int,
    var category_id: Int,
    var category_name: String,
    val measurement: String,
    val description: String,
    val dollar_rate: String
)

data class OrderDetails (
    val client_id: Int,
    val products: MutableList<ProductModel>,
//    val dollarRate: String
)

data class OrderModel (
    val id: Int,
    val date: String,
    val total: String,
    val status: Int,
    val products: List<ProductModel>
)

data class SimilarProducts (
    val product: ProductModel,
    val similarProducts: List<ProductModel>,
    val productImages: List<ProductImage>
)

data class ProductImage (
    val id: Int,
    val product_id: Int,
    val picture: String,
    val sort: Int
)

data class adsImages (
    val pictures: List<String>,
    val clientsCount: Int
)