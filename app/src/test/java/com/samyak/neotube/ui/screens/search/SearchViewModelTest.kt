package com.samyak.neotube.ui.screens.search

import com.samyak.neotube.data.repository.YouTubeRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: YouTubeRepository = mockk()
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SearchViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state is correct`() {
        val state = viewModel.uiState.value
        assertThat(state.query).isEmpty()
        assertThat(state.filters).isNull()
    }

    @Test
    fun `search updates uiState query`() {
        viewModel.search("test query")
        assertThat(viewModel.uiState.value.query).isEqualTo("test query")
    }

    @Test
    fun `clearSearch resets query`() {
        viewModel.search("test query")
        viewModel.clearSearch()
        assertThat(viewModel.uiState.value.query).isEmpty()
    }
}
