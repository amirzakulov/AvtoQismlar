package uz.almirab.avtoqismlar.models

import java.io.Serializable

data class CartItem(var product: ProductModel, var quantity: Int = 0): Serializable
