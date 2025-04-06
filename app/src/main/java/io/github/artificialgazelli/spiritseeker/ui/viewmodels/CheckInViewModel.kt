package com.example.spiritseeker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spiritseeker.data.model.CheckIn
import com.example.spiritseeker.data.model.CheckInSubcategory
import com.example.spiritseeker.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    val checkIns: StateFlow<List<CheckIn>> = repository.getAllCheckIns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Function to update the last check-in date for a subcategory
    fun updateCheckInDate(checkInName: String, subcategoryName: String, newDate: LocalDate) {
        viewModelScope.launch {
            val currentCheckIns = checkIns.value
            val targetCheckIn = currentCheckIns.find { it.name == checkInName } ?: return@launch

            val updatedSubcategories = targetCheckIn.subcategories.map { sub ->
                if (sub.name == subcategoryName) {
                    val nextDate = sub.intervalMonths?.let { newDate.plusMonths(it.toLong()) }
                    sub.copy(
                        lastDate = newDate.format(DateTimeFormatter.ISO_DATE),
                        nextDate = nextDate?.format(DateTimeFormatter.ISO_DATE)
                    )
                } else {
                    sub
                }
            }

            val updatedCheckIn = targetCheckIn.copy(subcategories = updatedSubcategories)
            repository.updateCheckIn(updatedCheckIn) // Assumes updateCheckIn exists in repo/DAO
        }
    }

    // TODO: Add functions for adding/editing check-in types or subcategories if needed

    // Function to update notes for a subcategory
    fun updateCheckInNotes(checkInName: String, subcategoryName: String, newNotes: String?) {
         viewModelScope.launch {
            val currentCheckIns = checkIns.value
            val targetCheckIn = currentCheckIns.find { it.name == checkInName } ?: return@launch

            val updatedSubcategories = targetCheckIn.subcategories.map { sub ->
                if (sub.name == subcategoryName) {
                    sub.copy(notes = newNotes?.trim())
                } else {
                    sub
                }
            }

            val updatedCheckIn = targetCheckIn.copy(subcategories = updatedSubcategories)
            repository.updateCheckIn(updatedCheckIn)
        }
    }
}