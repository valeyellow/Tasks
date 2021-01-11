package com.example.tasklist.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tasklist.R
import com.example.tasklist.databinding.FragmentAddEditTaskBinding
import com.example.tasklist.utils.exhaustive
import com.example.tasklist.viewmodel.AddEditTasksViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {
    val viewModel: AddEditTasksViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditTaskBinding.bind(view)

        binding
            .apply {
                editTextTaskName.setText(viewModel.taskName)
                checkboxPriority.isChecked = viewModel.taskImportance
                checkboxPriority.jumpDrawablesToCurrentState() // this removes the animation when the checkbox is checked

                // show the created by textview if viewModel.task != null
                textViewDateCreated.isVisible = viewModel.task != null
                textViewDateCreated.text =
                    "Created: ${viewModel.task?.createdDateFormatted}"

                editTextTaskName.addTextChangedListener { taskName ->
                    viewModel.taskName = taskName.toString()
                }

                checkboxPriority.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.taskImportance = isChecked
                }

                fabSaveTask.setOnClickListener {
                    viewModel.onSaveClick()
                }
            }


        // set the listener for addEditTask event listeners
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is AddEditTasksViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {

                        // show Snackbar with invalid input
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                    is AddEditTasksViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        binding.editTextTaskName.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                }.exhaustive
            }
        }
    }
}