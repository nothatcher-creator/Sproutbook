package com.nothatcher.sproutbook

import android.app.Application
import androidx.lifecycle.*
import com.nothatcher.sproutbook.core.ProfileRules
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

data class FamilyState(
    val loading: Boolean = true,
    val children: List<Child> = emptyList(),
    val child: Child? = null,
    val prefs: Preferences = Preferences(),
    val error: String? = null,
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FamilyViewModel(app: Application) : AndroidViewModel(app) {
    val repo = (app as BabyForgeApp).repository
    private val events = Channel<String>(Channel.BUFFERED)
    val messages = events.receiveAsFlow()
    private val retry = MutableStateFlow(0)
    val state =
        retry
            .flatMapLatest {
                combine(repo.db.children().observe(), repo.settings.flow) { children, prefs ->
                        val selected = ProfileRules.selected(prefs.selected, children.map { it.id })
                        if (selected != prefs.selected) repo.settings.select(selected)
                        val safeChildren = children.map {
                            if (it.stage in Stage.entries.map { stage -> stage.name }) it
                            else it.copy(stage = "BABY")
                        }
                        FamilyState(
                            false,
                            safeChildren,
                            safeChildren.find { it.id == selected },
                            prefs,
                        )
                    }
                    .catch {
                        emit(
                            FamilyState(
                                loading = false,
                                error =
                                    "Your records could not be opened. Your saved data has not been removed.",
                            )
                        )
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FamilyState())

    fun message(text: String) {
        viewModelScope.launch { events.send(text) }
    }

    fun retryOpen() {
        retry.value++
    }

    fun perform(success: String = "Saved", block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                if (success.isNotEmpty()) events.send(success)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                events.send(
                    if (e is IllegalArgumentException || e is IllegalStateException)
                        e.message ?: "Check your entry."
                    else "Could not save. Your existing records are safe. Please try again."
                )
            }
        }
    }

    fun select(id: String) = perform("") { repo.settings.select(id) }
}
