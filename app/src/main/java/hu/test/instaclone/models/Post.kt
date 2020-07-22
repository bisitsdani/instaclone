package hu.test.instaclone.models

import com.google.firebase.firestore.PropertyName

data class Post(
    var description: String = "",
    var created: Long = 0,
    @get:PropertyName("image_url") @set:PropertyName("image_url") var imageUrl: String = "",
    var user: User? = null
)