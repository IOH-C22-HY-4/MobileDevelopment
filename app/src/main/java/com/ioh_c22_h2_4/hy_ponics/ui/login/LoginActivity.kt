package com.ioh_c22_h2_4.hy_ponics.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.ioh_c22_h2_4.hy_ponics.ui.main.MainActivity
import com.ioh_c22_h2_4.hy_ponics.R
import com.ioh_c22_h2_4.hy_ponics.data.helper.UserPreference
import com.ioh_c22_h2_4.hy_ponics.databinding.ActivityLoginBinding
import com.ioh_c22_h2_4.hy_ponics.ui.ViewModelFactory
import com.ioh_c22_h2_4.hy_ponics.ui.signup.SignUpActivity
import com.ioh_c22_h2_4.hy_ponics.util.ApiCallbackString
import com.ioh_c22_h2_4.hy_ponics.util.isEmailValid

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

class LoginActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupViewModel()
        setMyButtonEnable()
        setSignUp()
        editTextListener()
        buttonListener()
        showLoading()
    }

    private fun setupViewModel() {
        loginViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[LoginViewModel::class.java]
    }

    private fun showLoading() {
        loginViewModel.isLoading.observe(this) {
            binding.apply {
                if (it) progressBar.visibility = View.VISIBLE
                else progressBar.visibility = View.GONE
            }
        }
    }

    private fun editTextListener() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setMyButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setMyButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

    }

    private fun setMyButtonEnable() {
        val resultPass = binding.etPassword.text
        val resultEmail = binding.etEmail.text

        binding.buttonLogin.isEnabled = resultPass != null && resultEmail != null &&
                binding.etPassword.text.toString().length >= 6 &&
                isEmailValid(binding.etEmail.text.toString())
    }

    private fun showAlertDialog(param: Boolean, message: String) {
        if (param) {
            AlertDialog.Builder(this).apply {
                setTitle(getString(R.string.information))
                setMessage(getString(R.string.login_success))
                setPositiveButton(getString(R.string.conti)) { _, _ ->
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                create()
                show()
            }
        } else {
            AlertDialog.Builder(this).apply {
                setTitle(getString(R.string.information))
                setMessage(getString(R.string.login_failed) + ", $message")
                setPositiveButton(getString(R.string.conti)) { _, _ ->
                    binding.progressBar.visibility = View.GONE
                }
                create()
                show()

            }
        }
    }

    private fun buttonListener() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()

            loginViewModel.login(email, pass, object : ApiCallbackString {
                override fun onResponse(success: Boolean, message: String) {
                    showAlertDialog(success, message)
                }
            })
        }
    }

    private fun setSignUp() {
        binding.signUp.setOnClickListener {
            register()
        }
    }

    private fun register() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }

}