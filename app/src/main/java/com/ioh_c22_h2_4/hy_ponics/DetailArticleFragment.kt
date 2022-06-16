package com.ioh_c22_h2_4.hy_ponics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentDetailArticleBinding

class DetailArticleFragment : Fragment() {

    private var _binding: FragmentDetailArticleBinding? = null
    private val binding get() = _binding!!

    private val args: DetailArticleFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.ivBack.setOnClickListener {
            it.findNavController().navigate(R.id.action_detailArticleFragment_to_homeFragment)
        }
        super.onViewCreated(view, savedInstanceState)
        val article = args.idArticle

        Glide.with(this)
            .load(article.img)
            .into(_binding?.ivArticleImage!!)
        _binding!!.tvTitleArticle.text = article.title
        _binding!!.tvDescription.text = article.content
        }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}