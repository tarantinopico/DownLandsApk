package com.example.ui.home

import com.example.domain.Category
import com.example.domain.FileItem

data class FileItemUI(
    val file: FileItem,
    val matchingCategories: List<Category>
)
