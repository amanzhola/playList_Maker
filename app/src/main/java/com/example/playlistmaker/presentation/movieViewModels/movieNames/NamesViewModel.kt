package com.example.playlistmaker.presentation.movieViewModels.movieNames

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.SingleLiveEvent
import com.example.playlistmaker.domain.api.moviePersons.NamesInteractor
import com.example.playlistmaker.domain.models.moviePerson.Person
import com.example.playlistmaker.utils.SEARCH_DEBOUNCE_DELAY
import com.example.playlistmaker.utils.collectDebouncedIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
class NamesViewModel(private val context: Context,
                     private val namesInteractor: NamesInteractor
) : ViewModel() {

    // Создаём StateFlow:
    private val searchQueryFlow = MutableStateFlow("")

    private val stateLiveData = MutableLiveData<NamesState>()
    fun observeState(): LiveData<NamesState> = stateLiveData

    private val showToast = SingleLiveEvent<String?>()
    fun observeShowToast(): LiveData<String?> = showToast

    init {
        searchQueryFlow
            .collectDebouncedIn(viewModelScope, SEARCH_DEBOUNCE_DELAY) { query ->
                searchRequest(query)
            }
    }

    fun searchDebounce(changedText: String) {
        searchQueryFlow.value = changedText
    }

    private fun searchRequest(newSearchText: String) {
        if (newSearchText.isNotEmpty()) {

            renderState(NamesState.Loading)

            viewModelScope.launch {
                namesInteractor
                    .searchNames(newSearchText)
                    .collect { pair ->
                        processResult(pair.first, pair.second)
                    }
            }
        }

    }

    private fun processResult(foundNames: List<Person>?, errorMessage: String?) {
        val persons = mutableListOf<Person>()
        if (foundNames != null) {
            persons.addAll(foundNames)
        }

        when {
            errorMessage != null -> {
                renderState(
                    NamesState.Error(
                        message = context.getString(
                            R.string.something_went_wrong
                        )
                    )
                )
                showToast.postValue(errorMessage)
            }
            persons.isEmpty() -> {
                renderState(NamesState.Empty(message = context.getString(R.string.nothing_found)))
            }
            else -> {
                renderState(NamesState.Content(persons = persons))
            }
        }
    }

    private fun renderState(state: NamesState) {
        stateLiveData.postValue(state)
    }
}