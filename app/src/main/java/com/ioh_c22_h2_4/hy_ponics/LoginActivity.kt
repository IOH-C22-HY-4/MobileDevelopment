package com.ioh_c22_h2_4.hy_ponics

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.ioh_c22_h2_4.hy_ponics.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var progressDialog: ProgressDialog

    private lateinit var auth: FirebaseAuth;

    private var email = ""
    private var password = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle(getString(R.string.wait))
        progressDialog.setMessage(getString(R.string.logging))
        progressDialog.setCanceledOnTouchOutside(false)

        binding.signUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.buttonLogin.setOnClickListener {
            validateData()
        }

    }


    private fun validateData() {
        binding.apply {
            email = etEmail.text.toString().trim()
            password = etPassword.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = getString(R.string.invalidEmail)
            } else if (TextUtils.isEmpty(password)) {
                etPassword.error = getString(R.string.enterPas)
            } else {
                firebaseLogin()
            }
        }

    }

    private fun firebaseLogin() {
        progressDialog.show()
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                val firebaseUser = auth.currentUser
                val email = firebaseUser!!.email
                Toast.makeText(this, "LoggedIn as $email", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "LoggedIn as ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}