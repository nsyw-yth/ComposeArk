package com.nsyw.nsyw_base.widget.banner

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.nsyw.nsyw_base.R
import kotlinx.coroutines.delay

@ExperimentalPagerApi
@Composable
fun Banner(
    list: MutableList<BannerData>,
    modifier: Modifier = Modifier,
    contentRadius: Float = 0f,
    timeMillis: Long = 3000,
    @DrawableRes loadImage: Int = R.drawable.base_banner_empty,
    indicatorAlignment: Alignment = Alignment.BottomCenter,
    onClick: (data: BannerData) -> Unit
) {
    val newList= mutableListOf<BannerData>()
    newList.addAll(list)
    newList.add(list[0])
    BannerIn(
        list = newList,
        modifier = modifier,
        contentRadius = contentRadius,
        timeMillis = timeMillis,
        loadImage = loadImage,
        indicatorAlignment = indicatorAlignment,
        onClick = onClick
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun BannerIn(
    list: MutableList<BannerData>?,
    modifier: Modifier = Modifier,
    contentRadius: Float = 0f,
    timeMillis: Long = 3000,
    @DrawableRes loadImage: Int = R.drawable.base_banner_empty,
    indicatorAlignment: Alignment = Alignment.BottomCenter,
    onClick: (data: BannerData) -> Unit
) {
    Box(
        modifier = modifier
    ) {

        if (list == null) {
            //加载中的图片
            Image(
                painterResource(loadImage),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape = RoundedCornerShape(16.dp)),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        } else {
            val pagerState = rememberPagerState(
                //初始页面
                initialPage = 0
            )

            //监听动画执行
            var executeChangePage by remember { mutableStateOf(false) }
            var currentPageIndex = 0

            //自动滚动
            LaunchedEffect(pagerState.currentPage, executeChangePage) {
                if (pagerState.pageCount > 0) {
                    //这里直接+1就可以循环，前提是infiniteLoop == true
                    if (pagerState.currentPage == list.size - 1) {
                        pagerState.scrollToPage(0)
                    } else {
                        delay(timeMillis)
                        pagerState.animateScrollToPage(
                            page = (pagerState.currentPage + 1) % (pagerState.pageCount),
                            pageOffset = 0f
                        )
//                        pagerState.scrollToPage((pagerState.currentPage + 1) % (pagerState.pageCount))
                    }
                }
            }

            HorizontalPager(
                count = list.size,
                state = pagerState,
                modifier = Modifier
                    .pointerInput(pagerState.currentPage) {
                        awaitPointerEventScope {
                            while (true) {
                                //PointerEventPass.Initial - 本控件优先处理手势，处理后再交给子组件
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                //获取到第一根按下的手指
                                val dragEvent = event.changes.firstOrNull()
                                when {
                                    //当前移动手势是否已被消费
                                    dragEvent!!.positionChangeConsumed() -> {
                                        return@awaitPointerEventScope
                                    }
                                    //是否已经按下(忽略按下手势已消费标记)
                                    dragEvent.changedToDownIgnoreConsumed() -> {
                                        //记录下当前的页面索引值
                                        currentPageIndex = pagerState.currentPage
                                    }
                                    //是否已经抬起(忽略按下手势已消费标记)
                                    dragEvent.changedToUpIgnoreConsumed() -> {
                                        //当页面没有任何滚动/动画的时候pagerState.targetPage为null，这个时候是单击事件
                                        if (pagerState.targetPage == null) return@awaitPointerEventScope
                                        //当pageCount大于1，且手指抬起时如果页面没有改变，就手动触发动画
                                        if (currentPageIndex == pagerState.currentPage && pagerState.pageCount > 1) {
                                            executeChangePage = !executeChangePage
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .clickable {
                        with(list[pagerState.currentPage]) {
                            onClick.invoke(this)
                        }
                    }
                    .fillMaxSize(),
            ) { page ->
                Image(
                    painter = rememberImagePainter(list[page].imgUrl),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape = RoundedCornerShape(contentRadius.dp)),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            }

            Box(
                modifier = Modifier
                    .align(indicatorAlignment)
                    .padding(bottom = 6.dp, start = 6.dp, end = 6.dp)
            ) {

                //指示点
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until list.size - 1) {
                        //大小
                        var size by remember { mutableStateOf(5.dp) }
                        size =
                            if (pagerState.currentPage == i || (pagerState.currentPage == list.size - 1 && i == 0)) 7.dp else 5.dp

                        //颜色
                        val color =
                            if (pagerState.currentPage == i || (pagerState.currentPage == list.size - 1 && i == 0)) MaterialTheme.colors.primary else Color.Gray

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(color)
                                //当size改变的时候以动画的形式改变
                                .animateContentSize()
                                .size(size)
                        )
                        //指示点间的间隔
                        if (i != list.lastIndex) Spacer(
                            modifier = Modifier
                                .height(0.dp)
                                .width(4.dp)
                        )
                    }
                }

            }
        }

    }
}