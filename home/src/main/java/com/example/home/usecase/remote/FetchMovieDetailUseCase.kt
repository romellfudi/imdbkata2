package com.example.home.usecase.remote

import com.example.data.models.MovieDetailResponse
import com.example.home.data.HomeFetchMoviesRepository
import com.example.home.data.api.IMDBState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FetchMovieDetailUseCase @Inject constructor(
    private val repository: HomeFetchMoviesRepository
) {
    operator fun invoke(id: Int): Flow<MovieDetailResponse> = flow {
        repository.fetchMovieDetail(id).collect {
            if (it is IMDBState.Success) {
                val movie = it.data
                emit(movie)
            }
        }
    }
}