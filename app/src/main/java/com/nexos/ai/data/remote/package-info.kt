/**
 * Remote data layer.
 *
 * In the MVP, AI providers talk to vendor APIs directly via [com.nexos.ai.ai.HttpAIClient]
 * (OkHttp), so there is no Retrofit interface here. The structure exists for future expansion
 * (e.g. a `NotesSyncApi` once cloud sync is added — see "Direction 1" in the architecture doc).
 */
@file:JvmName("RemotePackage")

package com.nexos.ai.data.remote
