package com.ioh_c22_h2_4.hy_ponics.ui.iot

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ioh_c22_h2_4.hy_ponics.R
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentIotBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IOTFragment : Fragment() {

    private var _binding: FragmentIotBinding? = null
    private val binding get() = _binding!!

    private val parameterAdapter by lazy { ParameterAdapter() }

    private val viewModel: IOTViewModel by viewModels()

    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sensorData.observe(viewLifecycleOwner) {
            Log.d(this.javaClass.simpleName, "$it")
            parameterAdapter.submitList(it)
        }

        binding.rvParameter.apply {
            adapter = parameterAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        auth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.btnProfileEdit.setOnClickListener {
            it.findNavController().navigate(R.id.action_IOTFragment_to_detailProfileFragment)
        }

        binding.settings.setOnClickListener {
            it.findNavController().navigate(R.id.action_IOTFragment_to_settingMenuFragment)
        }
        binding.btnChangeProfileIOT.setOnClickListener {
            it.findNavController().navigate(R.id.action_IOTFragment_to_detailProfileIOTFragment)
        }

    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(auth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = "${snapshot.child("username").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val address = "${snapshot.child("address").value}"
                    val usernameIOT = "${snapshot.child("usernameIOT").value}"
                    val profileImageIOT = "${snapshot.child("profileImageIOT").value}"


                    try {
                        Glide.with(this@IOTFragment)
                            .load(profileImageIOT)
                            .placeholder(R.drawable.add_photo)
                            .into(binding.ivIotResult)

                        Glide.with(this@IOTFragment)
                            .load(profileImage)
                            .placeholder(R.drawable.img_1)
                            .into(binding.ivProfileImage)

                        binding.tvProfileName.text = username
                        binding.tvProfileAddress.text = address
                        binding.tvNameIot.text = usernameIOT
                    }
                    catch (e: Exception){}

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}