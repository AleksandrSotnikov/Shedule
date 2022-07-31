package ru.mishenko.shedule.model

import com.google.gson.annotations.SerializedName

data class MainShedule(
    @SerializedName("week") var week: String? = null,
    @SerializedName("dow") var dow: String? = null,
    @SerializedName("group") var group: String? = null,
    @SerializedName("para") var para: String? = null,
    @SerializedName("pg") var pg: String? = null,
    @SerializedName("discipline") var discipline: String? = null,
    @SerializedName("teacher") var teacher: String? = null,
    @SerializedName("audience") var audience: String? = null
)

data class MainSheduleResult(
    @SerializedName("results") var results: ArrayList<MainShedule> = arrayListOf()
)

data class ReShedule(
    @SerializedName("group") var group: String? = null,
    @SerializedName("para") var para: String? = null,
    @SerializedName("pg") var pg: String? = null,
    @SerializedName("discipline") var discipline: String? = null,
    @SerializedName("teacher") var teacher: String? = null,
    @SerializedName("audience") var audience: String? = null
)

data class ReSheduleResult(
    @SerializedName("results") var results: ArrayList<ReShedule> = arrayListOf(),
    @SerializedName("date") var date: String? = null
)

data class ListReShedule(
    @SerializedName("results") var list: ArrayList<ReSheduleResult> = arrayListOf(),
)
