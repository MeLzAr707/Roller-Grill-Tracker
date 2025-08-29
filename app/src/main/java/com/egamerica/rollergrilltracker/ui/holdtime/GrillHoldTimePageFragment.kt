package com.egamerica.rollergrilltracker.ui.holdtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.egamerica.rollergrilltracker.databinding.FragmentGrillHoldTimePageBinding

class GrillHoldTimePageFragment : Fragment() {

    private var _binding: FragmentGrillHoldTimePageBinding? = null
    private val binding get() = _binding!!

    private var grillNumber: Int = 0
    private var grillName: String = ""

    companion object {
        private const val ARG_GRILL_NUMBER = "grill_number"
        private const val ARG_GRILL_NAME = "grill_name"

        fun newInstance(grillNumber: Int, grillName: String): GrillHoldTimePageFragment {
            val fragment = GrillHoldTimePageFragment()
            val args = Bundle()
            args.putInt(ARG_GRILL_NUMBER, grillNumber)
            args.putString(ARG_GRILL_NAME, grillName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            grillNumber = it.getInt(ARG_GRILL_NUMBER)
            grillName = it.getString(ARG_GRILL_NAME) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrillHoldTimePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupUI()
    }

    private fun setupRecyclerView() {
        binding.slotsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        // TODO: Set up adapter for slots when ready
    }

    private fun setupUI() {
        binding.grillNameText.text = grillName
        // TODO: Load slots for this grill
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}