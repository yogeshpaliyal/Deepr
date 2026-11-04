package com.yogeshpaliyal.deepr.update

/** Thrown when the user still has to allow this app to install unknown apps. */
class InstallPermissionRequiredException : Exception("Permission to install apps is required")
