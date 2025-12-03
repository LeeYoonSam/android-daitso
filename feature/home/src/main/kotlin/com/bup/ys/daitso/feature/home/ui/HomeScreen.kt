package com.bup.ys.daitso.feature.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bup.ys.daitso.core.model.Product
import com.bup.ys.daitso.feature.home.contract.HomeContract
import com.bup.ys.daitso.feature.home.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)

/**
 * HOME 화면의 메인 Composable
 *
 * MVI 아키텍처를 따르며, 다음 상태들을 렌더링합니다:
 * - Initial: 초기 상태 (빈 화면)
 * - Loading: 로딩 인디케이터
 * - Success: 상품 그리드
 * - Error: 에러 메시지
 *
 * @param viewModel HomeViewModel 인스턴스
 * @param onProductClick 상품 클릭 콜백
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onProductClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            HomeTopBar()
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
                        onProductClick = onProductClick
                    )
                }
                is HomeContract.HomeState.Error -> {
                    val state = uiState as HomeContract.HomeState.Error
                    ErrorView(message = state.message)
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
 * 성공 상태 뷰: 상품 그리드
 *
 * @param products 표시할 상품 리스트
 * @param onProductClick 상품 클릭 콜백
 */
@Composable
fun SuccessView(
    products: List<Product>,
    onProductClick: (String) -> Unit
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

/**
 * 에러 상태 뷰
 *
 * @param message 표시할 에러 메시지
 */
@Composable
fun ErrorView(message: String) {
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
