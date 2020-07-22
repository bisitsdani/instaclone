package hu.test.instaclone

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import hu.test.instaclone.models.Post
import hu.test.instaclone.models.User
import kotlinx.android.synthetic.main.activity_new_post.*

private const val TAG = "NewPostActivity"
//image pick code
private const val IMAGE_PICK_CODE = 1000;
//Permission code
private const val PERMISSION_CODE = 1001;
class NewPostActivity : AppCompatActivity() {

    private var photoUri: Uri? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private var signedInUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
        requestPermissions(permissions, PERMISSION_CODE)
        storageRef = FirebaseStorage.getInstance().reference
        firestoreDb = FirebaseFirestore.getInstance()
        getCurrentUser()
        btnPickImage.setOnClickListener {
            Log.i(TAG, "Picking image from gallery")
            pickImageFromGallery()

        }
        btnUpload.setOnClickListener {
            handleUpload()
        }
    }

    private fun handleUpload() {
        if(photoUri == null){
            Toast.makeText(this, "Nem választottál képet", Toast.LENGTH_SHORT).show()
            return
        }
        if(etDescription.text.isBlank()){
            Toast.makeText(this, "Adj hozzá leírást", Toast.LENGTH_SHORT).show()
            return
        }
        if(signedInUser == null){
            Toast.makeText(this, "Nem vagy bejelentkezve", Toast.LENGTH_SHORT).show()
            return
        }
        btnUpload.isEnabled = false
        val photoRef = storageRef.child("images/${System.currentTimeMillis()}-photo.jpg")
        photoRef.putFile(photoUri as Uri).continueWithTask { photoUploadTask ->
            Log.i(TAG, "uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
            photoRef.downloadUrl
        }.continueWithTask { downloadUrlTask ->
            val post = Post(
                etDescription.text.toString(),
                System.currentTimeMillis(),
                downloadUrlTask.result.toString(),
                signedInUser
            )
            firestoreDb.collection("posts").add(post)
        }.addOnCompleteListener { postCreationTask ->

            if(!postCreationTask.isSuccessful) {
                Log.e(TAG, "Upload to firestore failed", postCreationTask.exception)
                Toast.makeText(this, "Feltöltésnél hiba lépett fel", Toast.LENGTH_SHORT).show()
                btnUpload.isEnabled = true
                return@addOnCompleteListener
            }
            etDescription.text.clear()
            ivPickedImage.setImageResource(0)
            btnUpload.isEnabled = true
            Toast.makeText(this, "Sikeres feltöltés", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.name)
            startActivity(Intent(this, PostsActivity::class.java))
            finish()
        }

    }

    private fun getCurrentUser() {
        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String).get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
            }.addOnFailureListener { exception -> Log.e(TAG, "Error getting user", exception) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == IMAGE_PICK_CODE){
            if(resultCode == Activity.RESULT_OK){
                photoUri = data?.data
                ivPickedImage.setImageURI(photoUri)
            } else {
                Toast.makeText(this, "Kép választás visszavonva", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    //pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }
}