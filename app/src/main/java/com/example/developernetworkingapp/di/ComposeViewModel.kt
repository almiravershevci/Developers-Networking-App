package com.example.developernetworkingapp.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.developernetworkingapp.ui.viewmodel.ConversationViewModel

@Composable
inline fun <reified VM : ViewModel> appViewModel(
    key: String? = null,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
): VM = viewModel(
    viewModelStoreOwner = viewModelStoreOwner,
    key = key,
    factory = AppViewModelFactory(),
)

@Composable
fun conversationViewModel(conversationId: String): ConversationViewModel = viewModel(
    key = conversationId,
    factory = ConversationViewModelFactory(conversationId),
)
