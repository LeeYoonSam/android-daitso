package com.bup.ys.daitso.core.network

import com.bup.ys.daitso.core.model.Product
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API service interface for Daitso API.
 *
 * Defines endpoints for fetching products and product details.
 */
interface DaitsoApiService {

    /**
     * Fetches a list of all products.
     *
     * @return Call to retrieve list of products
     */
    @GET("products")
    fun getProducts(): Call<List<Product>>

    /**
     * Fetches details of a specific product.
     *
     * @param productId The ID of the product to fetch
     * @return Call to retrieve product details
     */
    @GET("products/{id}")
    fun getProduct(@Path("id") productId: String): Call<Product>
}
