package app.revanced.patches.twitch.debug.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object IsOmVerificationEnabledFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/BuildConfigUtil;") && methodDef.name == "isOmVerificationEnabled"
    }
)