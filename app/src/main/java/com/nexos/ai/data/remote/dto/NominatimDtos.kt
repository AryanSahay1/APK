package com.nexos.ai.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * OpenStreetMap Nominatim reverse-geocoding response (subset).
 * See https://nominatim.org/release-docs/develop/api/Reverse/.
 */
data class NominatimReverseDto(
    @SerializedName("display_name") val displayName: String? = null,
    val lat: String? = null,
    val lon: String? = null,
    val address: NominatimAddressDto? = null
)

data class NominatimAddressDto(
    @SerializedName("house_number") val houseNumber: String? = null,
    val road: String? = null,
    val neighbourhood: String? = null,
    val suburb: String? = null,
    val village: String? = null,
    val town: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postcode: String? = null,
    val country: String? = null,
    @SerializedName("country_code") val countryCode: String? = null
)
