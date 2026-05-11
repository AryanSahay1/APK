package com.nexos.ai.data.repository

import com.nexos.ai.data.local.dao.AlarmDao
import com.nexos.ai.data.local.entity.Alarm
import com.nexos.ai.service.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Source of truth for alarms. Coordinates the Room table with [AlarmScheduler] so that
 * inserting / updating / deleting an alarm always keeps the AlarmManager state in sync.
 */
@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao,
    private val scheduler: AlarmScheduler
) {

    fun observeAll(): Flow<List<Alarm>> = alarmDao.observeAll()

    suspend fun add(alarm: Alarm): Alarm = withContext(Dispatchers.IO) {
        val id = alarmDao.insert(alarm)
        val saved = alarm.copy(id = id)
        scheduler.schedule(saved)
        saved
    }

    suspend fun toggle(alarm: Alarm, enabled: Boolean) = withContext(Dispatchers.IO) {
        val updated = alarm.copy(isEnabled = enabled, isFired = if (enabled) false else alarm.isFired)
        alarmDao.update(updated)
        if (enabled) scheduler.schedule(updated) else scheduler.cancel(updated)
    }

    suspend fun delete(alarm: Alarm) = withContext(Dispatchers.IO) {
        scheduler.cancel(alarm)
        alarmDao.delete(alarm)
    }

    suspend fun markFired(id: Long) = withContext(Dispatchers.IO) {
        alarmDao.markFired(id)
    }

    /**
     * Re-schedule every pending alarm. Called from BootReceiver and from
     * the Settings exact-alarm toggle so user reminders survive reboots / permission changes.
     */
    suspend fun reschedulePending() = withContext(Dispatchers.IO) {
        alarmDao.getPending().forEach { scheduler.schedule(it) }
    }

    fun canScheduleExactAlarms(): Boolean = scheduler.canScheduleExactAlarms()
}
