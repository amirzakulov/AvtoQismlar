package uz.almirab.avtoqismlar.util

import android.content.Context
import io.paperdb.Paper
import uz.almirab.avtoqismlar.models.CartItem

class ShoppingCart {
    companion object {
        fun addItem(cartItem: CartItem) {
            val cart = this.getCart()

            val targetItem = cart.singleOrNull { it.product.id == cartItem.product.id }
            if (targetItem == null) {
                cartItem.quantity++
                cart.add(cartItem)
            } else {

                targetItem.product.count = cartItem.product.count
            }
            this.saveCart(cart)
        }

        fun removeItem(cartItem: CartItem, context: Context) {
            val cart = this.getCart()

            val targetItem = cart.singleOrNull { it.product.id == cartItem.product.id }
            if (targetItem != null) {
                cart.remove(targetItem)
            }

            this.saveCart(cart)
        }

        fun saveCart(cart: MutableList<CartItem>) {
            Paper.book().write("cart", cart)
        }

        fun getCart(): MutableList<CartItem> {
            return Paper.book().read("cart", mutableListOf())!!
        }

        fun getShoppingCartSize(): Int {
            var cartSize = 0
            this.getCart().forEach {
                cartSize += it.quantity;
            }

            return cartSize
        }

        fun changeItemCount(cartItem: CartItem) {
            val cart = this.getCart()

            val targetItem = cart.singleOrNull { it.product.id == cartItem.product.id }
            targetItem!!.product.count = cartItem.product.count

            this.saveCart(cart)
        }

        fun removeAll() {
            Paper.book().delete("cart");
        }
    }
}