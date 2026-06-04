# Presentation layer (MVVM)

```
ui/
├── navigation/     AppNavHost, routes, shell scaffold, AppNavigationViewModel
├── screens/        Route (wires VM) + Screen (pure Compose UI)
├── state/          UiState data classes
├── event/          One-shot navigation events
├── viewmodel/      ViewModels — business logic, no Compose/Android UI APIs
├── components/     Reusable UI
└── theme/

di/
├── AppContainer.kt          Repositories & data sources
├── ViewModelFactory.kt      Injects repositories into ViewModels
└── ComposeViewModel.kt      `appViewModel()` / `conversationViewModel()`
```

## Flow

1. **Route** composable obtains a ViewModel via `appViewModel()` and collects `uiState`.
2. **Screen** composable receives state + callbacks; it does not call repositories or `AppContainer`.
3. **ViewModel** reads/writes via repository interfaces; emits `UiState` and one-shot `*Event` / `*NavEvent` flows.
4. **Navigation** side effects are collected in Route/NavHost (`AuthNavHandler`, `AppNavigationViewModel`), not inside ViewModels with `NavController`.

## Session

`AppNavigationViewModel` guards authenticated routes. Auth/profile ViewModels emit navigation events; the nav layer performs `NavController` actions.
