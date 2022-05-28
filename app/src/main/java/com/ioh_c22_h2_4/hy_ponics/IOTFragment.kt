package com.ioh_c22_h2_4.hy_ponics

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentIotBinding

class IOTFragment : Fragment(),View.OnClickListener {

    private var _binding: FragmentIotBinding? = null
    private val binding get() = _binding!!

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
        val btnEditProfile: Button = view.findViewById(R.id.btnEditProfile)
        btnEditProfile.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btnEditProfile) {
            val mDetailProfileFragment = DetailProfileFragment()
            val mFragmentManager = parentFragmentManager
            mFragmentManager.beginTransaction().apply {
                replace(
                    R.id.IOTFragment,
                    mDetailProfileFragment,
                    DetailProfileFragment::class.java.simpleName
                )
                addToBackStack(null)
                commit()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
