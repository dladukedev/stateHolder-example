package com.dladukedev.stateholderexample

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Settings(
    val count: Int = 1,
    val description: String = "Default Description",
)

class StateHolderExampleViewModel: ViewModel() {
   private val _state = MutableStateFlow(Settings())
    val state = _state.asStateFlow()

   fun setCount(newCount: Int)     {
       _state.update { current -> current.copy(count = newCount) }
   }

    fun setDescription(newDescription: String)     {
        _state.update { current -> current.copy(description = newDescription) }
    }
}