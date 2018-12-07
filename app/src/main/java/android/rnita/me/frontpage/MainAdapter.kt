package android.rnita.me.frontpage

import android.rnita.me.frontpage.databinding.PostContentBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MainAdapter : RecyclerView.Adapter<MainAdapter.PostContentViewHolder>() {

    val posts = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostContentViewHolder =
        PostContentViewHolder(PostContentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: PostContentViewHolder, position: Int) {
        val post = posts[position]
        holder.binding.title.text = post
    }

    inner class PostContentViewHolder(val binding: PostContentBinding) : RecyclerView.ViewHolder(binding.root)
}