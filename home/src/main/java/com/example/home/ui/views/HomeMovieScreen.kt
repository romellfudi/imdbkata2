package com.example.home.ui.views

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.core.view.*
import com.example.core.view.compose.FailedAnimation
import com.example.core.view.compose.LoadingAnimation
import com.example.data.models.CastModel
import com.example.data.models.MovieDetailResponse
import com.example.home.R
import com.example.home.helpers.HomeState
import com.example.home.ui.dataview.MovieDetailView
import com.example.home.ui.viewmodels.HomeMovieViewModel

/**
 * @author @romellfudi
 * @date 2023-03-16
 * @version 1.0.a
 */
@Composable
fun HomeMovieScreen(
    backScreen: () -> Unit,
    viewModel: HomeMovieViewModel,
    id: Int,
    modifier: Modifier = Modifier
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val isLoading = remember { viewModel.isLoading }
    val movieDetailView by viewModel.movieDetail.collectAsState(null)

    LaunchedEffect("Load Movie Detail") {
        viewModel.fetchMovieDetail(id)
    }

    when (isLoading.value) {
        is HomeState.Loading -> {
            LoadingAnimation(modifier = Modifier.fillMaxSize())
        }
        is HomeState.Error -> {
            FailedAnimation(modifier = Modifier.fillMaxSize())
        }
        is HomeState.Ready -> {
            ConstraintLayout(
                modifier = modifier.fillMaxWidth()
            ) {
                MovieDetailContent(
                    movieDetailView = movieDetailView,
                    backScreen = backScreen
                )
            }
        }
    }
}

@Composable
fun MovieDetailContent(
    movieDetailView: MovieDetailView?,
    backScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberScrollState()
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(state)
    ) {
        val (toolbar, header, backdropImage, summary, firstDivider, followButton, secondDivider, cast, recommendation) = createRefs()
        MovieDetailToolbar(
            movieDetailView?.detail?.title ?: stringResource(R.string.movie_title_default),
            modifier = Modifier.constrainAs(toolbar) {
                linkTo(
                    start = parent.start,
                    end = parent.end
                )
                top.linkTo(parent.top)
                width = Dimension.fillToConstraints
            },
            backScreen = backScreen
        )
        movieDetailView?.detail?.let {
            MovieDetailHeader(
                it,
                modifier = Modifier.constrainAs(header) {
                    linkTo(
                        start = parent.start,
                        end = parent.end
                    )
                    top.linkTo(toolbar.bottom)
                    width = Dimension.fillToConstraints
                }
            )
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(movieDetailView?.detail?.tileUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.placeholder),
            contentDescription = "movie poster",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .height(196.dp)
                .constrainAs(backdropImage) {
                    linkTo(
                        start = parent.start,
                        end = parent.end
                    )
                    top.linkTo(header.bottom, padding_4)
                }
        )
        movieDetailView?.detail?.let {
            MovieDetailSummary(
                it,
                modifier = Modifier.constrainAs(summary) {
                    linkTo(
                        start = parent.start,
                        end = parent.end
                    )
                    top.linkTo(backdropImage.bottom, padding_4)
                    width = Dimension.fillToConstraints
                }
            )
        }
        Divider(
            modifier = Modifier
                .background(SeparatorColor)
                .height(0.5.dp)
                .constrainAs(firstDivider) {
                    linkTo(
                        start = parent.start,
                        end = parent.end,
                    )
                    bottom.linkTo(summary.bottom)
                }
        )
        Button(
            onClick = {

            },
            elevation = buttonNoElevation,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent,
                disabledBackgroundColor = Color.Transparent
            ),
            modifier = Modifier
                .constrainAs(followButton) {
                    linkTo(
                        start = parent.start,
                        startMargin = padding_4,
                        end = parent.end,
                        endMargin = padding_4
                    )
                    top.linkTo(firstDivider.bottom, padding_4)
                    width = Dimension.fillToConstraints
                }
        ) {
            Text(
                text = stringResource(R.string.add_to_my_list),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color1,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(
                        vertical = padding_4
                    )
            )
        }

        Divider(
            modifier = Modifier
                .background(SeparatorColor)
                .height(0.5.dp)
                .constrainAs(secondDivider) {
                    linkTo(
                        start = parent.start,
                        end = parent.end,
                    )
                    top.linkTo(followButton.bottom, padding_4)
                }
        )
        movieDetailView?.cast?.let {
            MovieDetailCast(
                cast = it,
                modifier = Modifier
                    .constrainAs(cast) {
                        linkTo(
                            start = parent.start,
                            end = parent.end,
                        )
                        top.linkTo(secondDivider.bottom, padding_4)
                        width = Dimension.fillToConstraints
                    }
            )
        }
        movieDetailView?.recommendation?.let {
            IMDBMovies(
                title = stringResource(R.string.recommendations),
                movies = movieDetailView.recommendation,
                goToDetail = {

                },
                modifier = Modifier.constrainAs(recommendation) {
                    val previous = when {
                        movieDetailView.cast.isEmpty() -> secondDivider.bottom
                        else -> cast.bottom
                    }
                    linkTo(
                        start = parent.start,
                        end = parent.end,
                    )
                    top.linkTo(previous,padding_4)
                    width = Dimension.fillToConstraints
                }
            )
        }
    }
}

@Composable
fun MovieDetailToolbar(
    title: String,
    modifier: Modifier,
    backScreen: () -> Unit
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val (back, titleRef, divider) = createRefs()
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = "icon",
            tint = Color.Black,
            modifier = Modifier
                .clickable { backScreen() }
                .constrainAs(back) {
                    start.linkTo(parent.start, padding_4)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.constrainAs(titleRef) {
                linkTo(
                    start = parent.start,
                    startMargin = padding_40,
                    end = parent.end,
                    endMargin = padding_40
                )
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        )
        Divider(
            modifier = Modifier
                .background(SeparatorColor)
                .height(0.5.dp)
                .constrainAs(divider) {
                    linkTo(
                        start = parent.start,
                        end = parent.end,
                    )
                    bottom.linkTo(parent.bottom)
                }
        )
    }
}

@Composable
fun MovieDetailHeader(
    movie: MovieDetailResponse,
    modifier: Modifier
) {
    ConstraintLayout(modifier = modifier) {
        val (titleBullet, originalTitle, id) = createRefs()
        ConstraintLayout(modifier = modifier.constrainAs(titleBullet) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(parent.top)
        }.fillMaxWidth()) {
            val (bullet, titleRef) = createRefs()
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .width(padding_6)
                    .height(padding_24)
                    .background(Color1)
                    .constrainAs(bullet) {
                        start.linkTo(parent.start, padding_24)
                        top.linkTo(parent.top, padding_16)
                    }
            )
            Text(
                text = movie.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.constrainAs(titleRef) {
                    linkTo(
                        start = bullet.end,
                        startMargin = padding_12,
                        end = parent.end,
                        endMargin = padding_24
                    )
                    top.linkTo(bullet.top)
                    bottom.linkTo(bullet.bottom)
                    width = Dimension.fillToConstraints
                }
            )
        }
        Text(
            text = "${movie.originalTitle} (titulo original)",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color3,
            fontSize = 10.sp,
            modifier = Modifier.constrainAs(originalTitle) {
                linkTo(
                    start = parent.start,
                    startMargin = padding_40,
                    end = parent.end,
                    endMargin = padding_24
                )
                top.linkTo(titleBullet.bottom, padding_4)
                width = Dimension.fillToConstraints
            }
        )
        Text(
            text = "ID: ${movie.id}",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color3,
            fontSize = 12.sp,
            modifier = Modifier.constrainAs(id) {
                linkTo(
                    start = parent.start,
                    startMargin = padding_40,
                    end = parent.end,
                    endMargin = padding_24
                )
                top.linkTo(originalTitle.bottom, padding_4)
                width = Dimension.fillToConstraints
            }
        )
    }
}

@Composable
fun MovieDetailSummary(
    movie: MovieDetailResponse,
    modifier: Modifier
) {
    ConstraintLayout(modifier = modifier) {
        val (poster, genres, star, ranking, summary) = createRefs()
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(movie.posterUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.placeholder),
            contentDescription = "movie poster",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .height(106.dp)
                .width(74.dp)
                .constrainAs(poster) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom, padding_16)
                    start.linkTo(parent.start, padding_24)
                }
        )
        val genresText = movie.genres.firstOrNull()?.name
        Text(
            text = genresText.orEmpty(),
            color = Color3,
            fontSize = 10.sp,
            modifier = Modifier
                .border(
                    BorderStroke(0.5.dp, Color3),
                    RoundedCornerShape(4.dp)
                )
                .padding(vertical = padding_6, horizontal = padding_4)
                .constrainAs(genres) {
                    top.linkTo(parent.top)
                    start.linkTo(poster.end, padding_16)
                }
        )
        Image(
            painter = painterResource(
                id = R.drawable.star
            ),
            contentDescription = "star",
            modifier = Modifier
                .constrainAs(star) {
                    start.linkTo(genres.end, padding_12)
                    top.linkTo(genres.top)
                    bottom.linkTo(genres.bottom)
                }
        )
        Text(
            text = movie.voteAverage.toString(),
            color = Color3,
            fontSize = 12.sp,
            modifier = Modifier
                .constrainAs(ranking) {
                    start.linkTo(star.end, 2.dp)
                    top.linkTo(genres.top)
                    bottom.linkTo(genres.bottom)
                }
        )
        Text(
            text = movie.overview.orEmpty(),
            color = Color.Black,
            fontSize = 14.sp,
            maxLines = 4,
            fontWeight = FontWeight.Normal,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .constrainAs(summary) {
                    start.linkTo(poster.end, padding_16)
                    end.linkTo(parent.end, padding_24)
                    top.linkTo(genres.bottom, padding_4)
                    width = Dimension.fillToConstraints
                }
        )
    }
}

@Composable
fun MovieDetailCast(
    cast: List<CastModel>,
    modifier: Modifier
) {
    val state = rememberLazyListState()
    ConstraintLayout(modifier = modifier) {
        val (castBullet) = createRefs()
        ConstraintLayout(modifier = modifier.constrainAs(castBullet) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(parent.top)
        }.fillMaxWidth()) {
            val (bullet, titleRef, listCast) = createRefs()
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .width(padding_6)
                    .height(padding_24)
                    .background(Color1)
                    .constrainAs(bullet) {
                        start.linkTo(parent.start, padding_24)
                        top.linkTo(parent.top, padding_16)
                    }
            )
            Text(
                text = "Reparto",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.constrainAs(titleRef) {
                    linkTo(
                        start = bullet.end,
                        startMargin = padding_12,
                        end = parent.end,
                        endMargin = padding_24
                    )
                    top.linkTo(bullet.top)
                    bottom.linkTo(bullet.bottom)
                    width = Dimension.fillToConstraints
                }
            )
            LazyRow(
                contentPadding = PaddingValues(vertical = padding_16),
                state = state,
                modifier = Modifier.constrainAs(listCast) {
                    linkTo(
                        start = parent.start,
                        startMargin = padding_16,
                        end = parent.end,
                        endMargin = padding_16
                    )
                    top.linkTo(titleRef.bottom, padding_4)
                    width = Dimension.fillToConstraints
                }
            ) {
                items(cast) {
                    MovieDetailCastItem(it)
                }
            }
        }
    }
}

@Composable
fun MovieDetailCastItem(
    castModel: CastModel,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier = modifier
            .padding(horizontal = padding_4)
    ) {
        val (poster, name, originalName) = createRefs()
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(castModel.profileUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.placeholder),
            contentDescription = "cast poster",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .height(106.dp)
                .width(74.dp)
                .constrainAs(poster) {
                    linkTo(
                        start = parent.start,
                        end = parent.end
                    )
                    top.linkTo(parent.top)
                }
        )
        Text(
            text = castModel.name,
            maxLines = 1,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Light,
            color = Color.Black,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .constrainAs(name) {
                    linkTo(
                        start = parent.start,
                        startMargin = padding_4,
                        end = parent.end,
                        endMargin = padding_4
                    )
                    top.linkTo(
                        poster.bottom,
                        padding_4
                    )
                    width = Dimension.fillToConstraints
                }
        )
        Text(
            text = castModel.originalName,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = 10.sp,
            fontWeight = FontWeight.Light,
            color = Color3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .constrainAs(originalName) {
                    linkTo(
                        start = parent.start,
                        startMargin = padding_4,
                        end = parent.end,
                        endMargin = padding_4
                    )
                    top.linkTo(
                        name.bottom
                    )
                    bottom.linkTo(
                        parent.bottom,
                        padding_4
                    )
                    width = Dimension.fillToConstraints
                }
        )
    }
}
