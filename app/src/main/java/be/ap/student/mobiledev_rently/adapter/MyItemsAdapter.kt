package be.ap.student.mobiledev_rently.adapter
import be.ap.student.mobiledev_rently.MyItemDetailFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import be.ap.student.mobiledev_rently.R
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.databinding.FragmentMyItemsBinding
import be.ap.student.mobiledev_rently.databinding.SingleItemMyItemsBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class MyItemsAdapter : RecyclerView.Adapter<MyItemsAdapter.MyItemsViewHolder>(){
    private var itemList = listOf<Item>()
    private lateinit var binding: SingleItemMyItemsBinding
    private lateinit var parentFragmentManager: FragmentManager

    fun submitList(items: List<Item>, parentFragmentManager: FragmentManager) {
        itemList = items
        notifyDataSetChanged()
        this.parentFragmentManager = parentFragmentManager
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemsViewHolder {
        // Use SingleItemMyItemsBinding for the single item layout
        binding = SingleItemMyItemsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyItemsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyItemsViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
        binding.editButton.setOnClickListener {
            Log.d("MyItemsAdapter", "Item clicked: ${item.getTitle()}")
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, MyItemDetailFragment.newInstance(item))
                .addToBackStack(null)
                .commit()
        }
    }

    override fun getItemCount(): Int = itemList.size

    inner class MyItemsViewHolder(private val binding: SingleItemMyItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            binding.itemName.text = item.getTitle()
            binding.itemPrice.text = "Price: $${item.getPrice()}"

            val imageUrl = item.getImage()
            if (imageUrl.isNullOrEmpty()) {
                Glide.with(binding.itemImage.context)
                    .load(R.drawable.default_item)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.itemImage)
            } else {
                Glide.with(binding.itemImage.context)
                    .load(imageUrl)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.itemImage)
            }
        }
    }
}
