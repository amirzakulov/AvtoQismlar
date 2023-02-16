package uz.almirab.avtoqismlar.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.paperdb.Paper
import uz.almirab.avtoqismlar.R
import uz.almirab.avtoqismlar.databinding.HistoryListBinding
import uz.almirab.avtoqismlar.models.Currency
import uz.almirab.avtoqismlar.models.OrderModel
import uz.almirab.avtoqismlar.util.DollarRate
import uz.almirab.avtoqismlar.util.MixFunctions

class HistoryAdapter(val context: Context?, private val payments: List<OrderModel>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
//    private val dollarRate: Currency = DollarRate.getDollarData()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapter.ViewHolder {
        return ViewHolder(HistoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: HistoryAdapter.ViewHolder, position: Int) {
        val payment = payments[position]
        holder.setData(payment, position)

    }

    override fun getItemCount() = payments.size

    inner class ViewHolder(private val binding: HistoryListBinding) : RecyclerView.ViewHolder(binding.root){

        //RecyclerView
        private lateinit var recyclerView: RecyclerView
        private lateinit var historyProductsAdapter: HistoryProductsAdapter

        @SuppressLint("ResourceAsColor")
        fun setData(order: OrderModel, position: Int) {

            var order_status = "Yangi"
            var order_status_color = R.color.red
            if(order.status == 1) {
                order_status = "Yo'lda"
                order_status_color = R.color.orange
            } else if (order.status == 2) {
                order_status = "Yopilgan"
                order_status_color = R.color.green

            }

            binding.historyDate.text = order.date
            binding.orderStatus.text = order_status
            binding.orderStatus.setTextColor(ContextCompat.getColor(context!!, order_status_color))

            Paper.init(context)
            var total:Int = 0
            order.products.forEach {
                val productPriceUzb = MixFunctions().roundPriceUp(it.sell_price.toDouble() * it.dollar_rate.toDouble())
                val productDiscountUzb = MixFunctions().roundPriceUp(it.discount.toDouble() * it.dollar_rate.toDouble())
                total += MixFunctions().roundPriceUp(( productPriceUzb - productDiscountUzb) * it.count.toDouble())
            }

            binding.historyTotal.text = context.getString(R.string.sum_title, MixFunctions().numberFormat(total))

            recyclerView                = binding.productsHistoryChild
            recyclerView.layoutManager  = LinearLayoutManager(context)
            historyProductsAdapter      = HistoryProductsAdapter(context, order.products)
            recyclerView.adapter        = historyProductsAdapter
        }
    }
}