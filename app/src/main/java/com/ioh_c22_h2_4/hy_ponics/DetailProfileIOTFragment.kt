@file:Suppress("DEPRECATION", "ControlFlowWithEmptyBody")

package com.ioh_c22_h2_4.hy_ponics

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentDetailProfileIOTBinding
import java.lang.Exception

class DetailProfileIOTFragment : Fragment() {
    private lateinit var binding: FragmentDetailProfileIOTBinding

    private lateinit var auth: FirebaseAuth

    private var imageUri: Uri? = null

    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailProfileIOTBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        loadUserInfo()

        progressDialog = ProgressDialog(requireActivity())
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnChangeProfileIOT.setOnClickListener {
            imageAttachment()
        }

        binding.btnSaveChangeIOT.setOnClickListener {
            validateData()
            val action = DetailProfileIOTFragmentDirections.actionDetailProfileIOTFragmentToIOTFragment()
            Navigation.findNavController(it).navigate(action)
        }

    }

    private var usernameIOT = ""

    private fun validateData() {
        usernameIOT = binding.etNameIot.text.toString().trim()

        if (usernameIOT.isEmpty()) {
            Toast.makeText(requireActivity(), "Enter your plant name", Toast.LENGTH_SHORT).show()
        } else {
            if (imageUri == null) {
                updateProfile("")
            } else {
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Uploading profile image")
        progressDialog.show()

        val filePathAndName = "ProfileImageIOT/" + auth.uid

        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadImageUrl = "${uriTask.result}"

                updateProfile(uploadImageUrl)


            }

            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    requireActivity(),
                    "Failed to upload image due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }
    }

    private fun updateProfile(uploadImageUrl: String) {
        progressDialog.setMessage("Updating Profile...")

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["usernameIOT"] = usernameIOT

        if (imageUri != null) {
            hashMap["profileImageIOT"] = uploadImageUrl
        }

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(auth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    requireActivity(),
                    "Profile IOT Updated",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    requireActivity(),
                    "Failed to update profile due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(auth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val usernameIOT = "${snapshot.child("usernameIOT").value}"
                    val profileImageIOT = "${snapshot.child("profileImageIOT").value}"

                    try {
                        Glide.with(this@DetailProfileIOTFragment)
                            .load(profileImageIOT)
                            .placeholder(R.drawable.add_photo)
                            .into(binding.imgProfileIOT)
                        binding.etNameIot.setText(usernameIOT)
                    } catch (e: Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun imageAttachment() {
        val popupMenu = PopupMenu(requireActivity(), binding.btnChangeProfileIOT)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if (id == 0) {
                pickImageGallery()
            }
            true

        }

    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                imageUri = data.data
            }

            binding.imgProfileIOT.setImageURI(imageUri)
        } else {
            Toast.makeText(requireActivity(), "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }


}