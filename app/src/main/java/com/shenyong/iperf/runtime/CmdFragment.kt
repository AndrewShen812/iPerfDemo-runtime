package com.shenyong.iperf.runtime

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.shenyong.iperf.runtime.databinding.CmdFragmentBinding

/**
 *
 * @author shenyong
 * @date 2020-11-10
 */
class CmdFragment : Fragment() {

    companion object {
        fun newInstance() = CmdFragment()
    }

    private lateinit var viewModel: CmdViewModel
    private lateinit var binding: CmdFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CmdFragmentBinding.inflate(layoutInflater)
        binding.btnIperf3Go.setOnClickListener {
            viewModel.iperfTest(requireContext())
        }
        // max: 200 Mbps
        binding.arcPannel.setMaxRange(100f)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CmdViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.addr.observe(viewLifecycleOwner, strObserver)
        viewModel.port.observe(viewLifecycleOwner, strObserver)
        viewModel.parallel.observe(viewLifecycleOwner, strObserver)
        viewModel.bandwidth.observe(viewLifecycleOwner, strObserver)
        viewModel.isDown.observe(viewLifecycleOwner, boolObserver)
        viewModel.isUdp.observe(viewLifecycleOwner, boolObserver)
        viewModel.bandwidthFloat.observe(viewLifecycleOwner, Observer {
            binding.arcPannel.setCurrentVal(it)
        })
    }

    private val strObserver = Observer<String> {
        viewModel.refreshCmdPreview(requireContext())
    }
    private val boolObserver = Observer<Boolean> {
        viewModel.refreshCmdPreview(requireContext())
    }

}
