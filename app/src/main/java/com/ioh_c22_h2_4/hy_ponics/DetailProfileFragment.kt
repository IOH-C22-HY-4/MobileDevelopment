package com.ioh_c22_h2_4.hy_ponics

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentDetailProfileBinding
import java.lang.Exception

class DetailProfileFragment : Fragment() {

    private lateinit var binding: FragmentDetailProfileBinding

    private lateinit var auth: FirebaseAuth

    private var imageUri: Uri? = null

    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        loadUserInfo()

        progressDialog = ProgressDialog(requireActivity())
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnChangeProfile.setOnClickListener {
            imageAttachment()
        }

        binding.btnSaveChange.setOnClickListener {
            validateData()
            val action = DetailProfileFragmentDirections.actionDetailProfileFragmentToIOTFragment()
            Navigation.findNavController(it).navigate(action)
        }

    }

    private var username = ""
    private var bio = ""
    private var address = ""
    private fun validateData() {
        username = binding.etName.text.toString().trim()
        bio = binding.etBio.text.toString().trim()
        address = binding.etAddress.text.toString().trim()

        if (username.isEmpty()) {
            Toast.makeText(requireActivity(), "Enter your name", Toast.LENGTH_SHORT).show()
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

        val filePathAndName = "ProfileImage/" + auth.uid

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
        hashMap["username"] = "$username"
        hashMap["bio"] = "$bio"
        hashMap["address"] = "$address"

        if (imageUri != null) {
            hashMap["profileImage"] = uploadImageUrl
        }

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(auth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    requireActivity(),
                    "Profile Updated",
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
                    val email = "${snapshot.child("email").value}"
                    val username = "${snapshot.child("username").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val bio = "${snapshot.child("bio").value}"
                    val address = "${snapshot.child("address").value}"

                    try {
                        Glide.with(this@DetailProfileFragment)
                            .load(profileImage)
                            .placeholder(R.drawable.img_1)
                            .into(binding.imgProfile)
                        binding.etName.setText(username)
                        binding.etEmail.setText(email)
                        binding.etBio.setText(bio)
                        binding.etAddress.setText(address)
                    } catch (e: Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun imageAttachment() {
        val popupMenu = PopupMenu(requireActivity(), binding.btnChangeProfile)
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
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    imageUri = data.data
                }

                binding.imgProfile.setImageURI(imageUri)
            } else {
                Toast.makeText(requireActivity(), "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )


}