package com.nexos.ai.presentation.navigation

/**
 * Flat navigation graph for the MVP.
 */
object NexosDestinations {
    const val NOTE_LIST = "noteList"
    const val SETTINGS = "settings"

    const val NOTE_DETAIL_ROUTE = "noteDetail/{noteId}"
    fun noteDetail(id: Long) = "noteDetail/$id"

    const val EDIT_NOTE_ROUTE = "editNote/{noteId}"
    fun editNote(id: Long) = "editNote/$id"
    const val NEW_NOTE_ID = -1L

    const val ARG_NOTE_ID = "noteId"
}
