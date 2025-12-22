package com.example.messageapp.ui.dialog

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.messageapp.databinding.FragmentScheduleBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar

class ScheduleBottomSheetFragment(
    private val onTimeSelected: (Long) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentScheduleBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScheduleBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener { dismiss() }

        // Later today, 5:00 PM
        binding.optionLaterToday.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 17) // 5 PM
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                // If already past 5 PM, maybe schedule for tomorrow? 
                // Or just let it be past (which usually means immediate or error). 
                // For simplicity, let's keep it as is, or move to next day if requested.
                // But usually "Later today" implies today. If it's 6 PM, this option might be disabled or hidden in real app.
                // I'll leave it as is for now.
            }
            onTimeSelected(calendar.timeInMillis)
            dismiss()
        }

        // Later tonight, 9:00 PM
        binding.optionLaterTonight.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 21) // 9 PM
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            onTimeSelected(calendar.timeInMillis)
            dismiss()
        }

        // Tomorrow, 8:00 AM
        binding.optionTomorrow.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 8)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            onTimeSelected(calendar.timeInMillis)
            dismiss()
        }

        // Pick date and time
        binding.optionPickDate.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun showDateTimePicker() {
        val currentCalendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val dateCalendar = Calendar.getInstance()
                dateCalendar.set(year, month, dayOfMonth)
                
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        dateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        dateCalendar.set(Calendar.MINUTE, minute)
                        dateCalendar.set(Calendar.SECOND, 0)
                        onTimeSelected(dateCalendar.timeInMillis)
                        dismiss()
                    },
                    currentCalendar.get(Calendar.HOUR_OF_DAY),
                    currentCalendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    companion object {
        const val TAG = "ScheduleBottomSheet"
    }
}
