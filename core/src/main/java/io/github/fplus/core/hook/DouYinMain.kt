package io.github.fplus.core.hook

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import com.freegang.extension.child
import com.freegang.extension.need
import com.freegang.ktutils.app.KActivityUtils
import com.freegang.ktutils.app.KAppCrashUtils
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.log.KLogCat
import io.github.fplus.Constant
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.core.helper.TimerExitHelper
import io.github.fplus.plugin.injectRes
import io.github.fplus.plugin.proxy.v1.PluginBridge
import io.github.xpler.core.KtXposedHelpers
import io.github.xpler.core.log.XplerLog
import java.util.zip.ZipFile

class DouYinMain(private val app: Application) {
    companion object {
        var timerExitHelper: TimerExitHelper? = null
        var freeExitHelper: TimerExitHelper? = null
    }

    init {
        runCatching {
            exportNative(app)

            // 插件化注入
            PluginBridge.init(app, "com.ss.android.ugc.aweme.setting.ui.AboutActivity")
            injectRes(app.resources)

            // 全局Application
            KAppUtils.setApplication(app)
            KActivityUtils.register(app)

            // 日志工具
            XplerLog.setTag("Freedom+")
            KLogCat.init(app)
            KLogCat.setTag("Freedom+")
            // KLogCat.silence() //静默

            // 全局异常捕获工具
            val intent = Intent()
            val className = "${Constant.modulePackage}.activity.ErrorActivity"
            intent.setClassName(Constant.modulePackage, className)
            KAppCrashUtils.init(app, "抖音异常退出!", intent)

            // 定时退出
            initTimedShutdown(app)

            // search and hook
            DexkitBuilder.running(
                app = app,
                version = 30,
                searchBefore = {
                    HPhoneWindow()
                    HActivity()
                    HMainActivity()
                    HDetailActivity()
                    HLandscapeFeedActivity()
                    HLivePlayActivity()
                    HDisallowInterceptRelativeLayout()
                    HMainTabStripScrollView()
                    HFlippableViewPager()
                    HPlayerController()
                    HPenetrateTouchRelativeLayout()
                    HInteractStickerParent()
                    HGifEmojiDetailActivity()
                    HEmojiDetailDialog()
                    HDialog()
                },
                searchAfter = {
                    HCrashTolerance()
                    HSideBarNestedScrollView()
                    HCornerExtensionsPopupWindow()
                    HMainBottomTabView()
                    HMainBottomPhotoTab()
                    HCommentListPageFragment()
                    HConversationFragment()
                    HSeekBarSpeedModeBottomMask()
                    HLongPressLayout()
                    HVideoViewHolder()
                    HFeedAvatarPresenter()
                    HHomeBottomTabServiceImpl()
                    HAbstractFeedAdapter()
                    HVerticalViewPager()
                    HDetailPageFragment()
                    HEmojiDetailDialogNew()
                    HEmojiPopupWindow()
                    HBottomCtrlBar()
                    HMessage()
                    HChatListRecyclerViewAdapter()
                    HChatListRecyclerViewAdapterNew()
                    HChatListRecalledHint()
                }
            )

        }.onFailure {
            XplerLog.e(it)
            KToastUtils.show(app, "Freedom+ Error: ${it.message}")
        }
    }

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    private fun exportNative(app: Application) {
        val libDir = ConfigV1.getConfigDir(app).child("lib").need()
        val libDexkit = libDir.child("libdexkit.so")
        val libMmkv = libDir.child("libmmkv.so")

        if (!libDexkit.exists() || !libMmkv.exists()) {
            val abi = if (KAppUtils.is64BitDalvik()) "arm64-v8a" else "armeabi-v7a"
            val dexkitSo = "lib/${abi}/libdexkit.so"
            val mmkbSo = "lib/${abi}/libmmkv.so"

            val zipFile = ZipFile(KtXposedHelpers.modulePath)
            zipFile.getInputStream(zipFile.getEntry(dexkitSo)).use { input ->
                libDexkit.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            zipFile.getInputStream(zipFile.getEntry(mmkbSo)).use { input ->
                libMmkv.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        System.load(libDexkit.absolutePath)
        ConfigV1.initialize(app) { _ ->
            System.load(libMmkv.absolutePath)
        }
    }

    @Synchronized
    private fun initTimedShutdown(app: Application) {
        val config = ConfigV1.get()
        if (!config.isTimedExit) {
            return
        }

        val timedExit = config.timedShutdownValue[0] * 60 * 1000L
        val freeExit = config.timedShutdownValue[1] * 60 * 1000L

        if (timedExit >= 60 * 1000L * 3) {
            timerExitHelper = TimerExitHelper(app, timedExit, config.keepAppBackend) {
                val second = it / 1000L
                if (second == 30L) {
                    KToastUtils.show(app, "抖音将在30秒后定时退出")
                }
                if (second <= 5) {
                    KToastUtils.show(app, "定时退出倒计时${second}s")
                }

                // KLogCat.d("定时退出进行中: ${second}s")
            }
        }

        if (freeExit >= 60 * 1000L * 3) {
            freeExitHelper = TimerExitHelper(app, freeExit, config.keepAppBackend) {
                val second = it / 1000L
                if (second == 30L) {
                    KToastUtils.show(app, "长时间无操作, 抖音将在30秒后空闲退出")
                }
                if (second <= 5) {
                    KToastUtils.show(app, "空闲退出倒计时${second}s")
                }

                // KLogCat.d("空闲退出进行中: ${second}s")
            }
        }
    }
}