package com.shenyong.iperf.runtime

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.shenyong.iperf.runtime.databinding.JniFragmentBinding

/**
 *
 * @author shenyong
 * @date 2020-11-10
 */
class JniFragment : Fragment() {

    companion object {
        fun newInstance() = JniFragment()
    }

    private lateinit var viewModel: JniViewModel
    private lateinit var binding: JniFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = JniFragmentBinding.inflate(layoutInflater)
        binding.btnIperf3Go.setOnClickListener {
            viewModel.iperfTest(requireContext())
        }
        // max: 200 Mbps
        binding.arcPannel.setMaxRange(200f)
        binding.arcPannel.setValUnitText("Mbps")
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(JniViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.bandwidthFloat.observe(viewLifecycleOwner, Observer {
            binding.arcPannel.setCurrentVal(it)
        })
    }
}
