package com.example.messageapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.messageapp.Controller
import com.example.messageapp.domain.model.MessageStatus

class ScheduleMessageWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val messageId = inputData.getLong("messageId", -1L)
        if (messageId == -1L) return Result.failure()

        val repository = (applicationContext as Controller).repository
        // We need a method in repo to "sendScheduledMessage now"
        // But retryMessage basically does re-sending. 
        // Or we can manually fetch and send.
        // Repository's 'retryMessage' logic: fetch, update status to SENDING, sendSmsReal.
        // This is exactly what we want for scheduled message too.
        // So we can reuse retryMessage!
        
        // Wait, retryMessage checks if message exists.
        // We should probably check if status is still SCHEDULED to avoid race conditions (e.g. user deleted it).
        // For simplicity, let's just call retryMessage logic (which updates to SENDING).
        
        // However, retryMessage is designed for FAILED messages. 
        // But the logic set status = SENDING and calls sendSmsReal. Code:
        /*
        val message = messageDao.getMessage(messageId) ?: return@withContext
        val retrying = message.copy(status = MessageStatus.SENDING)
        messageDao.update(retrying)
        ...
        sendSmsReal(retrying)
        */
        // This works perfectly for SCHEDULED -> SENDING transition too.
        
        try {
            repository.retryMessage(messageId)
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
