package de.seb.simpletodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import de.seb.simpletodo.ui.theme.SimpleToDoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleToDoApp()
        }
    }
}

@Composable
fun SimpleToDoApp(viewModel: ToDoViewModel = viewModel()) {
    SimpleToDoTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ToDoScreen(
                state = viewModel.state,
                onAddTodo = viewModel::addTodo,
                onMoveTodo = viewModel::moveTodo,
                onToggleTodo = viewModel::toggleTodo,
                onEditTodo = viewModel::startEditing,
                onDeleteTodo = viewModel::deleteTodo,
                onEditTextChange = viewModel::onEditTextChange,
                onEditImportantChange = viewModel::onEditImportantChange,
                onSaveEdit = viewModel::saveEdit,
                onCancelEdit = viewModel::cancelEdit,
            )
        }
    }
}
