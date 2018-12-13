package android.rnita.me.frontpage

import android.rnita.me.frontpage.databinding.PostContentBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MainAdapter : RecyclerView.Adapter<MainAdapter.PostContentViewHolder>() {

    val posts = mutableListOf<PostsQuery.Post>()

    var onItemClicked: ((Int) -> Unit)? = null
    var onButtonClicked: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostContentViewHolder =
        PostContentViewHolder(PostContentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: PostContentViewHolder, position: Int) {
        val post = posts[position].fragments().postFragment()
        holder.binding.title.text = post.title()
        holder.binding.name.text = "by ${post.author()?.firstName()} ${post.author()?.lastName()}"
        holder.binding.vote.text = "${post.votes()} votes"

        holder.binding.button.setOnClickListener {
            onButtonClicked?.invoke(post.id())
        }
        holder.binding.root.setOnClickListener {
            onItemClicked?.invoke(post.author()?.id() ?: return@setOnClickListener)
        }
    }

    inner class PostContentViewHolder(val binding: PostContentBinding) : RecyclerView.ViewHolder(binding.root)
}