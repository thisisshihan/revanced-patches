package app.revanced.patches.twitch.chat.antidelete.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object ChatUtilCreateDeletedSpanFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/ChatUtil\$Companion;") && methodDef.name == "createDeletedSpanFromChatMessageSpan"
    }
)