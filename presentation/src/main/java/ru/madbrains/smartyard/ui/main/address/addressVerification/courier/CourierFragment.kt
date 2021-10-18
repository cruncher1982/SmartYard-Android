package ru.madbrains.smartyard.ui.main.address.addressVerification.courier

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_courier.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.madbrains.smartyard.EventObserver
import ru.madbrains.smartyard.R
import ru.madbrains.smartyard.ui.main.MainActivity

class CourierFragment : Fragment() {

    private val viewModel by viewModel<CourierViewModel>()
    private var address = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_courier, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupObserve()

        arguments?.let {
            address = it.getString(ADDRESS_FIELD, "")
        }

        btnOk.setOnClickListener {
            viewModel.createIssue(address)
        }
    }

    private fun setupObserve() {
        viewModel.navigateToIssueSuccessDialogAction.observe(
            viewLifecycleOwner,
            EventObserver {
                (activity as MainActivity?)?.reloadToAddress()
            }
        )
    }

    companion object {
        fun getInstance(address: String): CourierFragment {
            val courierFragment = CourierFragment()
            val bundle = Bundle().apply {
                putString(ADDRESS_FIELD, address)
            }
            return courierFragment.apply {
                arguments = bundle
            }
        }
        const val ADDRESS_FIELD = "address_field"
    }
}
