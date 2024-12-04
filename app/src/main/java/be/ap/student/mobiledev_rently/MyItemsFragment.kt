package be.ap.student.mobiledev_rently

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import be.ap.student.mobiledev_rently.adapter.MyItemsAdapter
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentMyItemsBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import kotlinx.coroutines.launch


class MyItemsFragment : Fragment() {
    private lateinit var binding: FragmentMyItemsBinding
    private var user: User? = null
    private var userId: String? = null
    private lateinit var firebaseCommunication: FireBaseCommunication
    private lateinit var myItemsAdapter: MyItemsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getParcelable("user", User::class.java)
        }
        firebaseCommunication = FireBaseCommunication()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMyItemsBinding.inflate(inflater, container, false)
        val view = binding.root

        setupRecyclerView()
        user?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                userId = firebaseCommunication.getUserID(user!!.getEmail().toString())
                if (userId != null) {
                    loadUserItems(userId!!)
                }
            }
        }
        return view
    }

    private fun setupRecyclerView() {
        myItemsAdapter = MyItemsAdapter()
        binding.recyclerViewMyItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myItemsAdapter
        }
    }

    private fun loadUserItems(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val items = firebaseCommunication.getItemsByUser(userId)
                myItemsAdapter.submitList(items, parentFragmentManager)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Item list error", e.message.toString())
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(user: User?): MyItemsFragment {
            val fragment = MyItemsFragment()
            val args = Bundle()
            args.putParcelable("user", user)
            fragment.arguments = args
            return fragment
        }
    }

}