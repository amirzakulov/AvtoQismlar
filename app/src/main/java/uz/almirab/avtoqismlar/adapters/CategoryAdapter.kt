package uz.almirab.avtoqismlar.adapters

import android.content.Context
import android.content.Intent
import android.util.Log.d
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uz.almirab.avtoqismlar.ProductsActivity
import uz.almirab.avtoqismlar.api.BASE_URL
import uz.almirab.avtoqismlar.api.CATEGORY_IMAGE_URL
import uz.almirab.avtoqismlar.databinding.CategoryLayoutBinding
import uz.almirab.avtoqismlar.models.Categories

class CategoryAdapter(val context : Context?, private val categories: List<Categories>?) : RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        return MyViewHolder(CategoryLayoutBinding.inflate(LayoutInflater.from(p0.context), p0, false))

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val category = categories?.get(position)
        holder.setData(category!!, position)

    }

    override fun getItemCount() = categories!!.size

    inner class MyViewHolder (val binding: CategoryLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(category: Categories, position: Int) {

            Glide.with(context!!).load(BASE_URL+CATEGORY_IMAGE_URL+category.picture)
                .skipMemoryCache(false)
                .into(binding.categoryPhoto)

            binding.categoryTitle.text = category.name

            binding.category.setOnClickListener {
                val intent = Intent(context, ProductsActivity::class.java)
                intent.putExtra(ProductsActivity.SELECTED_CATEGORY, category)
                context.startActivity(intent)
            }
        }
    }

}