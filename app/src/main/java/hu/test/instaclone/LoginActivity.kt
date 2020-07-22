package hu.test.instaclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

private const val TAG = "LoginActivity"
class LoginActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        checkInstance()
        configLayout()
    }

    fun checkInstance(){
        if(auth.currentUser != null){
            goToPostsActivity()
        }
    }

    fun configLayout(){
        btnLogin.setOnClickListener {
            btnLogin.isEnabled = false
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()
            if(email.isBlank() || pass.isBlank()){
                Toast.makeText(this, "E-mail/Jelszó nem lehet üres", Toast.LENGTH_SHORT).show()
                btnLogin.isEnabled = true
                return@setOnClickListener
            }
            firebaseLogin(email, pass)
        }
    }

    fun firebaseLogin(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            btnLogin.isEnabled = true
            if(task.isSuccessful){
                Toast.makeText(this, "Sikeres belépés", Toast.LENGTH_SHORT).show()
                goToPostsActivity()
            }
            else{
                Toast.makeText(this, "Sikertelen belépés", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "signInWithEmail failed", task.exception)
            }
        }
    }
    fun goToPostsActivity(){
        startActivity(Intent(this, PostsActivity::class.java))
        finish() //Jelenlegi Activity bezárása
    }
}