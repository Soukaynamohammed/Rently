package be.ap.student.mobiledev_rently.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import be.ap.student.mobiledev_rently.R
import be.ap.student.mobiledev_rently.RentalDetailFragment
import be.ap.student.mobiledev_rently.dataClasses.Booking
import be.ap.student.mobiledev_rently.databinding.SingleBookingBinding
import com.bumptech.glide.Glide



class RentalsAdapter(private val parentFragmentManager: FragmentManager) : RecyclerView.Adapter<RentalsAdapter.RentalsViewHolder>() {

    private var bookingList = listOf<Booking>()

    fun submitList(bookings: List<Booking>) {
        bookingList = bookings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalsViewHolder {
        val binding = SingleBookingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RentalsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RentalsViewHolder, position: Int) {

        val booking = bookingList[position]
        holder.bind(booking)

        holder.binding.detailsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, RentalDetailFragment.newInstance(booking))
                .addToBackStack(null)
                .commit()
        }
    }

    override fun getItemCount(): Int = bookingList.size

    inner class RentalsViewHolder( val binding: SingleBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            binding.bookingItemName.text = booking.getItemName()
            binding.bookingDates.text = "${booking.getStartDate()} - ${booking.getEndDate()}"
            binding.bookingState.text = booking.getBookingState().toString()

            Glide.with(binding.bookingItemImage.context)
                .load(booking.getItemImage())
                .placeholder(R.drawable.default_item)
                .into(binding.bookingItemImage)
        }
    }
}
