package com.zulfahmi.simukomperawat.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "kumpulansoal")
data class Question(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Int? = null,

    @ColumnInfo(name = "jenis")
    val type: String,

    @ColumnInfo(name = "paket")
    val pack: Int,

    @ColumnInfo(name = "gambar")
    var image: String? = null,

    @ColumnInfo(name = "soal")
    val question: String,

    @ColumnInfo(name = "pilihan_a")
    val optionA: String,

    @ColumnInfo(name = "pilihan_b")
    val optionB: String,

    @ColumnInfo(name = "pilihan_c")
    val optionC: String,

    @ColumnInfo(name = "pilihan_d")
    val optionD: String,

    @ColumnInfo(name = "pilihan_e")
    val optionE: String,

    @ColumnInfo(name = "jawaban")
    val answer: String,

    @ColumnInfo(name = "pembahasan")
    val explanation: String? = "",

    @ColumnInfo(name = "gambar_pembahasan")
    val imageExplanation: String? = null

): Parcelable