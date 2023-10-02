package com.example.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


typealias Permission = String


data class PermissionResult(val permission: Permission, val granted: Boolean)
data class PermissionRationalResult(val permission: Permission, val showRational: Boolean)

sealed class PermissionsState {
    object Accepted : PermissionsState()
    data class Required(val permissions: List<Permission>) : PermissionsState()
}

sealed class PermissionRequestState {
    object Accepted : PermissionRequestState()
    data class ShouldShowRational(val permissions: List<PermissionRationalResult>) :
        PermissionRequestState()

    object PermanentDenied : PermissionRequestState()
}

fun Context.checkMultiplePermissions(permissions: Array<Permission>): List<PermissionResult> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        permissions.map { permission ->
            PermissionResult(
                permission = permission,
                granted = ContextCompat.checkSelfPermission(this, permission)
                        == PackageManager.PERMISSION_GRANTED
            )
        }
    else
        permissions.map { permission -> PermissionResult(permission = permission, granted = true) }

fun Context.permissionsState(permissions: Array<Permission>): PermissionsState =
    checkMultiplePermissions(permissions).run {
        if (granted) PermissionsState.Accepted
        else PermissionsState.Required(requiredPermissions)
    }

fun Context.checkPermission(permission: Permission): PermissionResult =
    PermissionResult(
        permission = permission,
        granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        else true
    )

fun Context.checkMultipleRational(result: List<Permission>): List<PermissionRationalResult> =
    result.map { permission ->
        PermissionRationalResult(
            permission,
            ActivityCompat.shouldShowRequestPermissionRationale(findActivity(), permission)
        )
    }

fun Context.permissionsRequestState(result: List<PermissionResult>): PermissionRequestState {
    val requiredPermissions = result.requiredPermissions
    return if (requiredPermissions.isEmpty()) PermissionRequestState.Accepted
    else {
        val rationalResult = checkMultipleRational(requiredPermissions)
        if (rationalResult.showRational) PermissionRequestState.ShouldShowRational(rationalResult)
        else PermissionRequestState.PermanentDenied
    }
}


val List<PermissionResult>.granted: Boolean
    get() = this.run {
        forEach { if (!it.granted) return@run false }
        return@run true
    }

val List<PermissionRationalResult>.showRational: Boolean
    get() = this.run {
        forEach { if (!it.showRational) return@run false }
        return@run true
    }

val List<PermissionResult>.requiredPermissions: List<Permission>
    get() = filter { !it.granted }.map { it.permission }

val List<PermissionResult>.denied: Boolean
    get() = this.run {
        forEach { if (it.granted) return@run false }
        return@run true
    }

val Map<String, @JvmSuppressWildcards Boolean>.permissionsResult: List<PermissionResult>
    get() = this.map { PermissionResult(it.key, it.value) }


fun Context.launchMobileSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri: Uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun Activity.requestPermissions(permissions: Array<Permission>) {
    ActivityCompat.requestPermissions(this, permissions, 0)
}


internal fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}