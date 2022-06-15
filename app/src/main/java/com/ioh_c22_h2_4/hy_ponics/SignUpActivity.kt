package com.ioh_c22_h2_4.hy_ponics

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ioh_c22_h2_4.hy_ponics.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private lateinit var progressDialog: ProgressDialog

    private lateinit var auth: FirebaseAuth;

    private var username = ""
    private var email = ""
    private var password = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle(getString(R.string.wait))
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnSignUp.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {

        username = binding.etName.text.toString().trim()
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()

        if (username.isEmpty()) {
            binding.etName.error = getString(R.string.enterName)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.invalidEmail)
        } else if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.enterPas)
        } else if (password.length < 6) {
            binding.etPassword.error = getString(R.string.pasMust6)
        } else {
            firebaseSignUp()
        }

    }

    private fun firebaseSignUp() {
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                updateUserInfo()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed Creating Account due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateUserInfo() {
        progressDialog.setMessage("Saving User Info...")

        val uid = auth.uid

        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["username"] = username
        hashMap["email"] = email
        hashMap["password"] = password
        hashMap["profileImage"] = "" //add empty, can do it in edit profile
        hashMap["bio"] = ""
        hashMap["address"] = ""

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Account Created...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed Creating Account due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }


}