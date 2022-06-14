package com.ioh_c22_h2_4.hy_ponics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ioh_c22_h2_4.hy_ponics.databinding.ItemArticleBinding

class ArticleAdapter(private val articleList: ArrayList<Article>) :
    RecyclerView.Adapter<ArticleAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = articleList[position]
        holder.bind(data)
    }

    class MyViewHolder(private val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Article) {
            Glide.with(binding.root.context)
                .load(data.img)
                .into(binding.articleImage)
            binding.articleTitle.text = data.title

            binding.root.setOnClickListener {
                val article = Article(
                    data.title,
                    data.content,
                    data.img
                )
                it.findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToDetailArticleFragment(article)
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return articleList.size
    }
}