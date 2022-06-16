package com.ioh_c22_h2_4.hy_ponics

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentSettingMenuBinding
import com.ioh_c22_h2_4.hy_ponics.util.Util.logout

class SettingMenuFragment : Fragment() {

    private var _binding: FragmentSettingMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSignOut.setOnClickListener {
            context?.let {
                AlertDialog.Builder(it).apply {
                    setTitle("Are you sure?")
                    setPositiveButton("Yes") { _, _ ->

                        FirebaseAuth.getInstance().signOut()
                        context.logout()

                    }
                    setNegativeButton("Cancel") { _, _ ->
                    }
                }.create().show()
            }
        }
        binding.about.setOnClickListener {
            it.findNavController().navigate(R.id.action_settingMenuFragment_to_aboutFragment2)
        }
        binding.theme.setOnClickListener {
            it.findNavController().navigate(R.id.action_settingMenuFragment_to_themeFragment)
        }
        super.onViewCreated(view, savedInstanceState)
        setupAction()
    }

    private fun setupAction() {
        binding.language.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}