package com.linda.gourmetdiary.data.source.remote

import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.linda.gourmetdiary.DiaryApplication
import com.linda.gourmetdiary.R
import com.linda.gourmetdiary.data.Diary
import com.linda.gourmetdiary.data.Result
import com.linda.gourmetdiary.data.Users
import com.linda.gourmetdiary.data.source.DiaryDataSource
import com.linda.gourmetdiary.util.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by Wayne Chen in Jul. 2019.
 *
 * Implementation of the Stylish source that from network.
 */
object DiaryRemoteDataSource : DiaryDataSource {

    private const val PATH_USERS = "Users"
    private const val PATH_STORESS = "Stores"
    private const val PATH_DIARYS = "diarys"
    private const val KEY_CREATED_TIME = "createdTime"

    override suspend fun getUsersDiarys(): Result<List<Diary>> = suspendCoroutine { continuation ->

        FirebaseFirestore.getInstance()
            .collection(PATH_USERS)
            .document("G1P80SW55MbkixdY69cx")
            .collection(PATH_DIARYS)
            .whereGreaterThanOrEqualTo("createdTime", 1594018156710)
            .whereLessThanOrEqualTo("createdTime", 1594018584070)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Logger.i("timestamp: " + document.data)
                    }
                } else {
                    task.exception?.let {
                        Logger.d("[${this::class.simpleName}] Error getting documents. ${it.message}")
                    }
                }
            }

        FirebaseFirestore.getInstance()
            .collection(PATH_USERS)
            .document("G1P80SW55MbkixdY69cx")
            .collection(PATH_DIARYS)
            .orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val list = mutableListOf<Diary>()
                    for (document in task.result!!) {
                        Logger.d(document.id + "=>" + document.data)

                        val diary = document.toObject(Diary::class.java)
                        list.add(diary)
                    }
                    continuation.resume(Result.Success(list))
                } else {
                    task.exception?.let {

                        Logger.d("[${this::class.simpleName}] Error getting documents. ${it.message}")
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(DiaryApplication.instance.getString(R.string.ng_msg)))
                }
            }
    }


    override suspend fun postDiary(diarys: Diary): Result<Boolean> =
        suspendCoroutine { continuation ->
            val diary = FirebaseFirestore.getInstance().collection(PATH_USERS)
                .document("G1P80SW55MbkixdY69cx")
                .collection(PATH_DIARYS)
            val stores = FirebaseFirestore.getInstance().collection(PATH_STORESS)
                .document()
            val document = diary.document()

            diarys.diaryId = document.id
            diarys.createdTime = Calendar.getInstance().timeInMillis

            diarys.store?.let {
                stores
                    .set(it)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Logger.i("stores: ${diarys.store}")

                        } else {
                            task.exception?.let {
                                Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                                continuation.resume(Result.Error(it))
                                return@addOnCompleteListener
                            }
                            continuation.resume(Result.Fail(DiaryApplication.instance.getString(R.string.ng_msg)))
                        }
                    }
                document
                    .set(diarys)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Logger.i("diary: $diarys")
                            continuation.resume(Result.Success(true))
                        } else {
                            task.exception?.let {

                                Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                                continuation.resume(Result.Error(it))
                                return@addOnCompleteListener
                            }
                            continuation.resume(Result.Fail(DiaryApplication.instance.getString(R.string.ng_msg)))
                        }
                    }
            }
        }

    override fun getLiveDiary(): MutableLiveData<List<Diary>> {
        val liveData = MutableLiveData<List<Diary>>()

        FirebaseFirestore.getInstance()
            .collection(PATH_USERS)
            .document("G1P80SW55MbkixdY69cx")
            .collection(PATH_DIARYS)
            .orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->

                Logger.i("addSnapshotListener detect")

                exception?.let {
                    Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                }

                val list = mutableListOf<Diary>()
                for (document in snapshot!!) {
                    Logger.d(document.id + " => " + document.data)

                    val liveDiary = document.toObject(Diary::class.java)
                    list.add(liveDiary)
                }

                liveData.value = list
            }
        return liveData
    }
}
