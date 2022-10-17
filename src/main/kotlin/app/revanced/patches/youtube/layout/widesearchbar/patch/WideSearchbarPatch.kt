package app.revanced.patches.youtube.layout.widesearchbar.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.BytecodeContext
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.widesearchbar.annotations.WideSearchbarCompatibility
import app.revanced.patches.youtube.layout.widesearchbar.fingerprints.IsInOfflineModeCheckFingerprint
import app.revanced.patches.youtube.layout.widesearchbar.fingerprints.IsInOfflineModeCheckResultFingerprint
import app.revanced.patches.youtube.layout.widesearchbar.fingerprints.SetWordmarkHeaderFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("enable-wide-searchbar")
@Description("Replaces the search icon with a wide search bar. This will hide the YouTube logo when active.")
@WideSearchbarCompatibility
@Version("0.0.1")
class WideSearchbarPatch : BytecodePatch(
    listOf(
        SetWordmarkHeaderFingerprint, IsInOfflineModeCheckFingerprint
    )
) {
    private companion object {
        /**
         * Walk a fingerprints method at a given index mutably.
         *
         * @param index The index to walk at.
         * @param fromFingerprint The fingerprint to walk the method on.
         * @return The [MutableMethod] which was walked on.
         */
        fun BytecodeContext.walkMutable(index: Int, fromFingerprint: MethodFingerprint) =
            fromFingerprint.result?.let {
                toMethodWalker(it.method).nextMethod(index, true).getMethod() as MutableMethod
            } ?: throw SetWordmarkHeaderFingerprint.toErrorResult()


        /**
         * Injects instructions required for certain methods.
         *
         */
        fun MutableMethod.injectSearchBarHook() {
            addInstructions(
                implementation!!.instructions.size - 1,
                """
                    invoke-static {}, Lapp/revanced/integrations/patches/NewActionbarPatch;->getNewActionBar()Z
                    move-result p0
                """
            )
        }
    }

    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_wide_searchbar",
                StringResource("revanced_wide_searchbar_enabled_title", "Enable wide search bar"),
                false,
                StringResource("revanced_wide_searchbar_summary_on", "Wide search bar is enabled"),
                StringResource("revanced_wide_searchbar_summary_off", "Wide search bar is disabled")
            )
        )

        // resolve fingerprints
        IsInOfflineModeCheckFingerprint.result?.let {
            if (!IsInOfflineModeCheckResultFingerprint.resolve(context, it.classDef))
                return IsInOfflineModeCheckResultFingerprint.toErrorResult()
        } ?: return IsInOfflineModeCheckFingerprint.toErrorResult()

        // patch methods
        mapOf(
            SetWordmarkHeaderFingerprint to 1,
            IsInOfflineModeCheckResultFingerprint to 0
        ).forEach { (fingerprint, callIndex) ->
            context.walkMutable(callIndex, fingerprint).injectSearchBarHook()
        }

        return PatchResult.Success
    }
}
