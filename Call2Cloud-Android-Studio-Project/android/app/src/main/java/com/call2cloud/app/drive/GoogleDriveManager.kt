package com.call2cloud.app.drive

import android.content.Context
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class GoogleDriveManager(private val context: Context) {
    companion object {
        val SCOPE = Scope(DriveScopes.DRIVE_FILE)
    }

    data class DriveUploadResult(val fileId: String, val md5: String, val isVerified: Boolean)

    fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(SCOPE)
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getSignedInAccount(): GoogleSignInAccount? {
        val acc = GoogleSignIn.getLastSignedInAccount(context)
        return if (acc != null && GoogleSignIn.hasPermissions(acc, SCOPE)) acc else null
    }

    suspend fun uploadAndVerify(
        file: File,
        filename: String,
        format: String,
        timestamp: Long,
        folderName: String = "Call2Cloud"
    ): DriveUploadResult = withContext(Dispatchers.IO) {
        val account = getSignedInAccount() ?: throw IllegalStateException("Drive not signed in")
        val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_FILE)).apply {
            selectedAccount = account.account
        }
        val drive = Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory.getDefaultInstance(), credential)
            .setApplicationName("Call2Cloud")
            .build()

        // Organize into Call2Cloud / YYYY / Month
        val year = SimpleDateFormat("yyyy", Locale.US).format(Date(timestamp))
        val month = SimpleDateFormat("MMMM", Locale.US).format(Date(timestamp))
        val rootId = getOrCreateFolder(drive, folderName, null)
        val yearId = getOrCreateFolder(drive, year, rootId)
        val monthId = getOrCreateFolder(drive, month, yearId)

        val meta = DriveFile().apply {
            name = filename
            parents = listOf(monthId)
        }
        val mime = if (format == "OPUS") "audio/ogg; codecs=opus" else "audio/mp4"
        val uploaded = drive.files().create(meta, FileContent(mime, file))
            .setFields("id, md5Checksum, size")
            .execute()

        val localMd5 = calculateMd5(file)
        val verified = uploaded.md5Checksum?.equals(localMd5, true) ?: (uploaded.size == file.length())

        if (!verified) throw IllegalStateException("Checksum verification failed")

        DriveUploadResult(uploaded.id, uploaded.md5Checksum ?: localMd5, true)
    }

    private fun getOrCreateFolder(drive: Drive, name: String, parentId: String?): String {
        val q = if (parentId == null) "mimeType = 'application/vnd.google-apps.folder' and name = '$name' and trashed = false and 'root' in parents"
        else "mimeType = 'application/vnd.google-apps.folder' and name = '$name' and trashed = false and '$parentId' in parents"
        val list = drive.files().list().setQ(q).setSpaces("drive").setFields("files(id)").execute()
        if (list.files.isNotEmpty()) return list.files[0].id
        val f = DriveFile().apply {
            this.name = name
            mimeType = "application/vnd.google-apps.folder"
            if (parentId != null) parents = listOf(parentId)
        }
        return drive.files().create(f).setFields("id").execute().id
    }

    private fun calculateMd5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        FileInputStream(file).use { fis ->
            val buf = ByteArray(8192)
            var n: Int
            while (fis.read(buf).also { n = it } != -1) md.update(buf, 0, n)
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}