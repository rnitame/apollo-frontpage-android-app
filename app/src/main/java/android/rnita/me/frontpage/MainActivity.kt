package android.rnita.me.frontpage

import android.os.Bundle
import android.rnita.me.frontpage.databinding.MainActivityBinding
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.rx2.Rx2Apollo
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class MainActivity : AppCompatActivity() {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()
    private val apolloClient = ApolloClient.builder()
        .okHttpClient(okHttpClient)
        .defaultHttpCachePolicy(HttpCachePolicy.CACHE_FIRST)
        .serverUrl("http://10.0.2.2:8080/graphql")
        .subscriptionTransportFactory(
            WebSocketSubscriptionTransport.Factory(
                "http://10.0.2.2:8090",
                okHttpClient
            )
        )
        .build()
    private val disposables = CompositeDisposable()
    private val adapter = MainAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity)

        adapter.onItemClicked = { authorId ->
            getAuthor(authorId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = {
                        val author = it.fragments().authorFragment()
                        AlertDialog.Builder(this)
                            .setTitle("Author of the post is ${author.firstName()} ${author.lastName()}.")
                            .setMessage("Posts count: ${author.posts()?.size}")
                            .setCancelable(true)
                            .show()
                    },
                    onError = {
                        Log.d("getAuthor", it.message)
                        Toast.makeText(this, "Failure getting author", Toast.LENGTH_SHORT).show()
                    }
                )
                .addTo(disposables)
        }
        adapter.onButtonClicked = {
            postUpvote(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = {
                        refreshList()
                    },
                    onError = {
                        Log.d("postUpvote", it.message)
                        Toast.makeText(this, "Failure posting vote", Toast.LENGTH_SHORT).show()
                    }
                )
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        refreshList()

//        subscribePostUpvoted()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribeBy(
//                onNext = { postUpvoted ->
//                    val post = postUpvoted.fragments().postFragment()
//                    val index = adapter.posts.indexOfFirst { it.fragments().postFragment().id() == post.id() }
//
//                    adapter.notifyItemChanged(index)
//                },
//                onError = {
//                    Log.d("subscribePostUpvoted", it.message)
//                    Toast.makeText(this, "Failure subscribing postUpvoted", Toast.LENGTH_SHORT).show()
//                }
//            )
//            .addTo(disposables)

    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    private fun refreshList() {
        getPosts()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    adapter.posts.clear()
                    adapter.posts.addAll(it)
                    adapter.notifyDataSetChanged()
                },
                onError = {
                    Log.d("getPosts", it.message)
                    Toast.makeText(this, "Failure getting posts", Toast.LENGTH_SHORT).show()
                }
            )
            .addTo(disposables)

    }

    // API client

    private fun getAuthor(id: Int): Single<AuthorQuery.Author> =
        Rx2Apollo.from(apolloClient.query(AuthorQuery.builder().id(id).build()))
            .map { it.data()?.author()!! }
            .singleOrError()

    private fun getPosts(): Single<List<PostsQuery.Post>> =
        Rx2Apollo.from(apolloClient.query(PostsQuery.builder().build()))
            .map { it.data()?.posts()!! }
            .singleOrError()

    private fun postUpvote(postId: Int): Single<UpvotePostMutation.UpvotePost> =
        Rx2Apollo.from(apolloClient.mutate(UpvotePostMutation.builder().postId(postId).build()))
            .map { it.data()?.upvotePost()!! }
            .singleOrError()

//    private fun subscribePostUpvoted(): Flowable<PostUpvotedSubscription.PostUpvoted> =
//        Rx2Apollo.from(apolloClient.subscribe(PostUpvotedSubscription.builder().build()))
//            .map { it.data()?.postUpvoted()!! }
}
