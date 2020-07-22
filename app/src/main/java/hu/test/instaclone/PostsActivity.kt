package hu.test.instaclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hu.test.instaclone.models.Post
import hu.test.instaclone.models.User
import kotlinx.android.synthetic.main.activity_posts.*

private const val TAG = "PostsActivity"
const val EXTRA_USERNAME = "EXTRA_USERNAME"

open class PostsActivity : AppCompatActivity() {

    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var posts: MutableList<Post>
    private lateinit var adapter: PostsAdapter
    private var signedInUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
        posts = mutableListOf()
        adapter = PostsAdapter(this, posts)
        rvPosts.adapter = adapter
        rvPosts.layoutManager = LinearLayoutManager(this)
        firestoreDb = FirebaseFirestore.getInstance()
        getCurrentUser()
        configDatabaseListener()

        fabCreate.setOnClickListener {
            val intent = Intent(this, NewPostActivity::class.java)
            startActivity(intent)
        }
    }

    private fun configDatabaseListener() {
        var ref = firestoreDb.collection("posts").orderBy("created", Query.Direction.DESCENDING)
        val username = intent.getStringExtra(EXTRA_USERNAME)
        if (username != null) {
            supportActionBar?.title = username
            ref = ref.whereEqualTo("user.name", username)
        }
        ref.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "Error when reading posts", exception)
                return@addSnapshotListener
            }
            val postsList = snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postsList)
            adapter.notifyDataSetChanged()
        }
    }

    private fun getCurrentUser() {
        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String).get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
            }.addOnFailureListener { exception -> Log.e(TAG, "Error getting user", exception) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_profile) {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.name)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}