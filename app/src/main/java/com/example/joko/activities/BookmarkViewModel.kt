package com.example.joko.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.joko.data.local.entity.BookmarkEventEntity
import com.example.joko.data.local.entity.BookmarkTeamEntity
import com.example.joko.data.local.entity.EventEntity
import com.example.joko.data.remote.response.TeamResponse
import com.example.joko.data.repository.EventRepository
import com.example.joko.data.repository.TeamRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class BookmarkViewModel(
    private val eventRepository: EventRepository,
    private val teamRepository: TeamRepository
) : ViewModel() {

    val bookmarkedEvents = eventRepository.allBookmarks.map { bookmarks ->
        bookmarks.map { it.toEventEntity() }
    }.asLiveData()

    val bookmarkedTeams = teamRepository.allBookmarks.map { bookmarks ->
        bookmarks.map { it.toTeamResponse() }
    }.asLiveData()

    fun toggleBookmark(event: EventEntity, isBookmarked: Boolean) {
        viewModelScope.launch {
            if (isBookmarked) {
                val bookmark = BookmarkEventEntity(
                    id = event.id,
                    title = event.title,
                    organizer = event.organizer,
                    category = event.category,
                    location = event.location,
                    startDate = event.startDate,
                    endDate = event.endDate,
                    description = event.description,
                    imageUrl = event.imageUrl,
                    registrationUrl = event.registrationUrl,
                    tags = event.tags,
                    requirements = event.requirements,
                    ownerId = event.ownerId,
                    isVerified = event.isVerified,
                    trustScore = event.trustScore
                )
                eventRepository.removeBookmark(bookmark)
            } else {
                eventRepository.addBookmark(event)
            }
        }
    }

    fun toggleBookmark(team: TeamResponse, isBookmarked: Boolean) {
        viewModelScope.launch {
            if (isBookmarked) {
                val bookmark = BookmarkTeamEntity(
                    id = team.id,
                    teamName = team.teamName,
                    eventName = team.eventName,
                    ownerId = team.ownerId,
                    maxCapacity = team.maxCapacity,
                    currentMembersCount = team.currentMembersCount,
                    description = team.description,
                    roleNeed = team.roleNeed?.joinToString(","),
                    ownerContact = team.ownerContact
                )
                teamRepository.removeBookmark(bookmark)
            } else {
                teamRepository.addBookmark(team)
            }
        }
    }

    private fun BookmarkEventEntity.toEventEntity(): EventEntity {
        return EventEntity(
            id = id,
            title = title,
            organizer = organizer,
            category = category,
            location = location,
            startDate = startDate,
            endDate = endDate,
            description = description,
            imageUrl = imageUrl,
            registrationUrl = registrationUrl,
            tags = tags,
            requirements = requirements,
            ownerId = ownerId,
            isVerified = isVerified,
            trustScore = trustScore
        )
    }

    private fun BookmarkTeamEntity.toTeamResponse(): TeamResponse {
        return TeamResponse(
            id = id,
            teamName = teamName,
            eventName = eventName,
            ownerId = ownerId,
            maxCapacity = maxCapacity,
            currentMembersCount = currentMembersCount,
            description = description,
            roleNeed = roleNeed?.split(",")?.filter { it.isNotBlank() },
            ownerContact = ownerContact
        )
    }
}
