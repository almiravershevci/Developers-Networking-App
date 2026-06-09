package com.example.developernetworkingapp.di

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.developernetworkingapp.ui.viewmodel.ConversationViewModel

/** Activity-scoped VMs survive navigation tab switches and match [CollectAuthNavEvents]. */
@Composable
inline fun <reified VM : ViewModel> activityViewModel(key: String? = null): VM {
    val activity = checkNotNull(LocalActivity.current as? ComponentActivity) {
        "No ComponentActivity in composition"
    }
    return appViewModel(viewModelStoreOwner = activity, key = key)
}

/** Activity-scoped auth VMs so login/signup screens share state with [CollectAuthNavEvents]. */
@Composable
inline fun <reified VM : ViewModel> authViewModel(key: String? = null): VM = activityViewModel(key)

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
