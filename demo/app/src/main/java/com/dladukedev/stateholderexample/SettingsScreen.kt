package com.dladukedev.stateholderexample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
data class DialogState<T>(
    val isShown: Boolean,
    val show: () -> Unit,
    val dismiss: () -> Unit,
    val confirm: () -> Unit,
    val current: T,
    val setCurrent: (T) -> Unit,
)

@Stable
data class SettingsScreenState(
    val settings: Settings,
    val snackbarHostState: SnackbarHostState,
    val countDialog: DialogState<Int>,
    val descriptionDialog: DialogState<String>,
)

@Composable
fun rememberSettingScreenState(
    settings: Settings,
    setCount: (Int) -> Unit,
    setDescription: (String) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): SettingsScreenState {
    var countDialogValue by rememberSaveable {
        mutableStateOf(settings.count)
    }
    var isCountDialogShown by rememberSaveable { mutableStateOf(false) }
    var descriptionDialogValue by rememberSaveable {
        mutableStateOf(settings.description)
    }
    var isDescriptionDialogShown by rememberSaveable { mutableStateOf(false) }

    val showCountDialog = remember(settings.count) {
        {
            countDialogValue = settings.count
            isCountDialogShown = true
        }
    }
    val countDialog = remember(isCountDialogShown, countDialogValue, settings.count) {
        DialogState(
            isShown = isCountDialogShown,
            show = showCountDialog,
            dismiss = { isCountDialogShown = false },
            confirm = {
                setCount(countDialogValue)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Updated Count")
                }
                isCountDialogShown = false
            },
            current = countDialogValue,
            setCurrent = { countDialogValue = it }
        )
    }
    val showDescriptionDialog = remember(settings.description) {
        {
            descriptionDialogValue = settings.description
            isDescriptionDialogShown = true
        }
    }
    val descriptionDialog =
        remember(isDescriptionDialogShown, descriptionDialogValue, settings.description) {
            DialogState(
                isShown = isDescriptionDialogShown,
                show = showDescriptionDialog,
                dismiss = { isDescriptionDialogShown = false },
                confirm = {
                    setDescription(descriptionDialogValue)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Updated Description")
                    }
                    isDescriptionDialogShown = false
                },
                current = descriptionDialogValue,
                setCurrent = { descriptionDialogValue = it }
            )
        }

    return remember(settings, countDialog, descriptionDialog) {
        SettingsScreenState(
            settings = settings,
            snackbarHostState = snackbarHostState,
            countDialog = countDialog,
            descriptionDialog = descriptionDialog,
        )
    }
}

@Composable
fun SettingsScreen(viewModel: StateHolderExampleViewModel = viewModel()) {
    val settings by viewModel.state.collectAsState()
    val state = rememberSettingScreenState(
        settings = settings,
        setCount = viewModel::setCount,
        setDescription = viewModel::setDescription
    )

    SettingsScreen(state)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: SettingsScreenState) {
    Scaffold(snackbarHost = { SnackbarHost(state.snackbarHostState) }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                )
            )
            SettingsRow(
                title = "Count",
                subTitle = state.settings.count.toString(),
                onClick = state.countDialog.show
            )
            SettingsRow(
                title = "Description",
                subTitle = state.settings.description,
                onClick = state.descriptionDialog.show
            )
        }
    }
    CountDialog(dialogState = state.countDialog)
    DescriptionDialog(dialogState = state.descriptionDialog)
}

@Composable
fun SettingsRow(
    title: String,
    subTitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            )
            .fillMaxWidth()
    ) {
        Text(text = title, style = MaterialTheme.typography.labelMedium)
        Text(text = subTitle, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun CountDialog(
    dialogState: DialogState<Int>,
) {
    SettingsDialog(title = "Count", dialogState = dialogState) {
        Column(modifier = Modifier.selectableGroup()) {
            (1..5).forEach {
                val setCount = remember(it) {
                    { dialogState.setCurrent(it) }
                }
                RadioButtonRow(
                    label = "$it",
                    isSelected = dialogState.current == it,
                    onClick = setCount,
                )
            }
        }
    }
}

@Composable
fun RadioButtonRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .selectable(
                selected = isSelected,
                onClick = onClick,
            )
            .padding(
                vertical = 8.dp,
                horizontal = 16.dp,
            )
            .fillMaxWidth()
    ) {
        RadioButton(selected = isSelected, onClick = null)
        Spacer(modifier = Modifier.width(24.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescriptionDialog(
    dialogState: DialogState<String>,
) {
    SettingsDialog(title = "Description", dialogState = dialogState) {
        TextField(
            label = { Text("Description") },
            value = dialogState.current,
            onValueChange = dialogState.setCurrent,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun <T> SettingsDialog(
    dialogState: DialogState<T>,
    title: String,
    confirmButtonText: String = "Ok",
    cancelButtonText: String? = null,
    content: @Composable ColumnScope.(DialogState<T>) -> Unit,
) {
    if (dialogState.isShown) {
        Dialog(onDismissRequest = dialogState.dismiss) {
            Surface(
                modifier = Modifier.defaultMinSize(360.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(
                            horizontal = 16.dp, vertical = 8.dp
                        )
                    )
                    content(dialogState)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            alignment = Alignment.End
                        ),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    ) {
                        if (cancelButtonText != null) {
                            Button(onClick = dialogState.dismiss) {
                                Text(text = cancelButtonText)
                            }
                        }
                        TextButton(onClick = dialogState.confirm) {
                            Text(text = confirmButtonText)
                        }
                    }
                }
            }
        }
    }
}
