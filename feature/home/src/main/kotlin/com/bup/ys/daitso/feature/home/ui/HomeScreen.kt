package com.bup.ys.daitso.feature.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bup.ys.daitso.core.model.Product
import com.bup.ys.daitso.feature.home.contract.HomeContract
import com.bup.ys.daitso.feature.home.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)

/**
 * HOME 화면의 메인 Composable
 *
 * MVI 아키텍처를 따르며, 다음 상태들을 렌더링합니다:
 * - Initial: 초기 상태 (빈 화면)
 * - Loading: 로딩 인디케이터
 * - Success: 상품 그리드 with Pull-to-Refresh
 * - Error: 에러 메시지 with Retry 버튼
 *
 * @param viewModel HomeViewModel 인스턴스
 * @param onProductClick 상품 클릭 콜백
 * @param onNavigateToDetail 상품 상세 화면 네비게이션 콜백
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onProductClick: (String) -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val sideEffect by viewModel.sideEffect.collectAsState(null)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // SideEffect 처리
    LaunchedEffect(sideEffect) {
        when (sideEffect) {
            is HomeContract.HomeSideEffect.NavigateToProductDetail -> {
                val productId = (sideEffect as HomeContract.HomeSideEffect.NavigateToProductDetail).productId
                onNavigateToDetail(productId)
            }
            is HomeContract.HomeSideEffect.ShowToast -> {
                val message = (sideEffect as HomeContract.HomeSideEffect.ShowToast).message
                snackbarHostState.showSnackbar(message)
            }
            is HomeContract.HomeSideEffect.ShowError -> {
                val message = (sideEffect as HomeContract.HomeSideEffect.ShowError).message
                snackbarHostState.showSnackbar(message)
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar()
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is HomeContract.HomeState.Initial -> {
                    InitialView()
                }
                is HomeContract.HomeState.Loading -> {
                    LoadingView()
                }
                is HomeContract.HomeState.Success -> {
                    val state = uiState as HomeContract.HomeState.Success
                    SuccessView(
                        products = state.products,
                        isRefreshing = state.isRefreshing,
                        onProductClick = { productId ->
                            coroutineScope.launch {
                                viewModel.submitEvent(HomeContract.HomeEvent.OnProductClick(productId))
                            }
                        },
                        onRefresh = {
                            coroutineScope.launch {
                                viewModel.submitEvent(HomeContract.HomeEvent.RefreshProducts)
                            }
                        }
                    )
                }
                is HomeContract.HomeState.Error -> {
                    val state = uiState as HomeContract.HomeState.Error
                    ErrorView(
                        message = state.message,
                        onRetry = {
                            coroutineScope.launch {
                                viewModel.submitEvent(HomeContract.HomeEvent.RetryLoad)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * HOME 화면의 상단 바
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    TopAppBar(
        title = {
            Text("홈", style = MaterialTheme.typography.headlineSmall)
        }
    )
}

/**
 * 초기 상태 뷰
 */
@Composable
fun InitialView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "상품을 로드하기 위해 화면을 새로고침하세요",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * 로딩 상태 뷰
 */
@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * 성공 상태 뷰: 상품 그리드 with Pull-to-Refresh
 *
 * @param products 표시할 상품 리스트
 * @param isRefreshing Pull-to-Refresh 중 여부
 * @param onProductClick 상품 클릭 콜백
 * @param onRefresh 새로고침 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessView(
    products: List<Product>,
    isRefreshing: Boolean = false,
    onProductClick: (String) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (products.isEmpty()) {
            EmptyView()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) }
                    )
                }
            }
        }
    }
}

/**
 * 에러 상태 뷰 with Retry 버튼
 *
 * @param message 표시할 에러 메시지
 * @param onRetry 재시도 콜백
 */
@Composable
fun ErrorView(message: String, onRetry: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "오류 발생",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("재시도")
            }
        }
    }
}

/**
 * 빈 상태 뷰: 상품이 없을 때
 */
@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "사용 가능한 상품이 없습니다",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
