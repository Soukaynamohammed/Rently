package be.ap.student.mobiledev_rently.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import be.ap.student.mobiledev_rently.ItemDetailFragment
import be.ap.student.mobiledev_rently.MyItemDetailFragment
import be.ap.student.mobiledev_rently.R
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.databinding.SingleItemMyItemsBinding
import be.ap.student.mobiledev_rently.databinding.SingleItemSearchItemsBinding
import com.bumptech.glide.Glide

class SearchAdapter(private val parentFragmentManager: FragmentManager) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private var itemList = listOf<Item>()

    fun submitList(items: List<Item>) {
        itemList = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = SingleItemSearchItemsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = itemList.size

    inner class SearchViewHolder(private val binding: SingleItemSearchItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            binding.itemName.text = item.getTitle()
            binding.itemPrice.text = "Price: $${item.getPrice()}"

            Glide.with(binding.itemImage.context)
                .load(item.getImage() ?: R.drawable.default_item)
                .into(binding.itemImage)

            binding.editButton.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, ItemDetailFragment.newInstance(item))
                    .addToBackStack(null)
                    .commit()
            }

            binding.root.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, ItemDetailFragment.newInstance(item))
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}
