package com.mnfarzaneh.solalrchef.data

import com.mnfarzaneh.solalrchef.data.remote.ApiService
import com.mnfarzaneh.solalrchef.data.remote.dto.ArticleDto
import com.mnfarzaneh.solalrchef.data.remote.dto.HeroArticleDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getHero(): Result<HeroArticleDto?> = runCatching {
        val response = api.getHeroArticle()
        when {
            response.code() == 204 -> null
            response.isSuccessful -> response.body()
            else -> error("خطا در دریافت محتوای پیشنهادی")
        }
    }

    suspend fun getArticles(limit: Int = 20): Result<List<HeroArticleDto>> = runCatching {
        val response = api.getArticles(limit)
        if (response.isSuccessful) {
            response.body().orEmpty()
        } else {
            error("خطا در دریافت مقاله‌های اخیر")
        }
    }

    suspend fun getArticle(slug: String): Result<ArticleDto> = runCatching {
        val response = api.getArticle(slug)
        if (response.isSuccessful) {
            response.body() ?: error("محتوای مقاله خالی است")
        } else {
            error(if (response.code() == 404) "این مقاله در دسترس نیست" else "خطا در دریافت مقاله")
        }
    }
}
