package be.ap.student.mobiledev_rently.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import be.ap.student.mobiledev_rently.MyItemDetailFragment
import be.ap.student.mobiledev_rently.R
import be.ap.student.mobiledev_rently.dataClasses.Booking
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.databinding.SingleItemMyItemsBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import be.ap.student.mobiledev_rently.util.BookingState
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.LinkedList

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
            var availability = "Unavailable"
            if(!(item.getStartDate()==""||item.getEndDate()=="")){
                if(LocalDate.now()>=LocalDate.parse(item.getStartDate())&&LocalDate.now()<=LocalDate.parse(item.getEndDate())){
                    availability = "Available"
                    var bookings: List<Booking> = LinkedList()
                    runBlocking {
                        launch {
                            bookings = FireBaseCommunication().getBookingsByItem(item)
                        }
                            .join()
                    }

                    bookings.forEach {
                        if(it.getBookingState()== BookingState.ACCEPTED && LocalDate.now()>=LocalDate.parse(it.getStartDate())&&LocalDate.now()<=LocalDate.parse(it.getEndDate())) {
                            availability = "Unavailable"
                        }
                    }
                }
            } else {
                availability = "Unavailable"
            }

            if(availability=="Unavailable"){
                binding.itemAvailability.setTextColor(getColor(binding.itemAvailability.context))
            }
            binding.itemAvailability.text = availability

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
    private fun getColor(ctx: Context) = ctx.resources.getColor(R.color.error, ctx.theme)
}
