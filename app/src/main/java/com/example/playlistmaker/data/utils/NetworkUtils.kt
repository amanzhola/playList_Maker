//package com.example.playlistmaker.data.utils
//
//import android.content.Context
//import android.net.ConnectivityManager
//import android.net.NetworkCapabilities
//
//object NetworkUtils {
//    fun isNetworkAvailable(context: Context): Boolean {
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//        return networkCapabilities != null && (
//                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
//                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
//                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
//                )
//    }
//}
////// добавлен за счет практикума NetworkCapabilities.TRANSPORT_ETHERNET
////package com.example.playlistmaker.data.utils
////
////import android.content.Context
////import android.net.ConnectivityManager
////import android.net.NetworkCapabilities
////
////object NetworkUtils {
////    fun isNetworkAvailable(context: Context): Boolean {
////        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
////        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
////        return networkCapabilities != null && (
////                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
////                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
////                )
////    }
////}