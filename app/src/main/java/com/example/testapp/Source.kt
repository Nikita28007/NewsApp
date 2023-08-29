package com.example.testapp

import java.lang.reflect.Constructor

class Source(
    val category: String,
    val country: String,
    val description: String,
    val id: String,
    val name: String,
    val url: String

    ) {
    override fun toString(): String {
        return "$name , $description, category: $category, $url"
    }
}

