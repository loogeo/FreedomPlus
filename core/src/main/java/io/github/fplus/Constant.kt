package io.github.fplus

import io.github.xpler.core.wrapper.at

object Constant {
    val modulePackage = "io.github.fplus"

    val scopes = setOf(
        "com.ss.android.ugc.aweme" at ("com.ss.android.ugc.aweme.app.host.AwemeHostApplication" to "com.ss.android.ugc.aweme"),
        "com.ss.android.ugc.aweme.lite" at ("com.ss.android.ugc.aweme.app.host.AwemeHostApplication" to "com.ss.android.ugc.aweme.lite"),
        "com.ss.android.ugc.live" at ("com.ss.android.ugc.aweme.app.host.AwemeHostApplication" to "com.ss.android.ugc.live"),
        "com.ss.android.ugc.awemf" at ("com.ss.android.ugc.aweme.app.host.AwemeHostApplication" to "com.ss.android.ugc.awemf"), // test
        "com.ss.android.ugc.awemg" at ("com.ss.android.ugc.aweme.app.host.AwemeHostApplication" to "com.ss.android.ugc.awemg"), // test
    )

}