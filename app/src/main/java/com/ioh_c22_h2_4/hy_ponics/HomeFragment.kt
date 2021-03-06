package com.ioh_c22_h2_4.hy_ponics

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentHomeBinding
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var articleArrayList: ArrayList<Article>
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.rvPopularArticle
        recyclerView.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)

        articleArrayList = arrayListOf()

        articleAdapter = ArticleAdapter(articleArrayList)
        recyclerView.adapter = articleAdapter

        binding.rvPopularArticle.setOnClickListener {
            it.findNavController().navigate(R.id.action_homeFragment_to_detailArticleFragment)
        }
        EventChangeListener()

        auth = FirebaseAuth.getInstance()
        loadUserInfo()
    }

    private fun EventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("poparticle").addSnapshotListener(object : EventListener<QuerySnapshot> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null) {
                    Log.e("Firestore error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        articleArrayList.add(dc.document.toObject(Article::class.java))
                    }
                }
                articleAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(auth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = "${snapshot.child("username").value}"
                    try {
                        binding.helloUser.text = username
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