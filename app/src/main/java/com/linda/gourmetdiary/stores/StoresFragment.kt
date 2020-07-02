package com.linda.gourmetdiary.stores

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.linda.gourmetdiary.R

class StoresFragment : Fragment() {

    companion object {
        fun newInstance() = StoresFragment()
    }

    private lateinit var viewModel: StoresViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.stores_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(StoresViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
