package com.nyro.browser.browser

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdBlocker @Inject constructor() {

    private val blockList = setOf(
        "doubleclick.net", "googlesyndication.com", "adservice.google.com",
        "ads.yahoo.com", "advertising.com", "adnxs.com", "rubiconproject.com",
        "pubmatic.com", "openx.net", "casalemedia.com", "criteo.com",
        "taboola.com", "outbrain.com", "revcontent.com", "mgid.com",
        "scorecardresearch.com", "quantserve.com", "chartbeat.com",
        "amazon-adsystem.com", "media.net", "bidswitch.net",
        "smartadserver.com", "yieldmo.com", "indexexchange.com",
        "appnexus.com", "33across.com", "triplelift.com", "sharethrough.com",
        "trafficjunky.net", "exoclick.com", "propellerads.com",
        "popcash.net", "hilltopads.net", "adcash.com", "juicyads.com",
        "ero-advertising.com", "traffichunt.com", "adsterra.com",
        "clickadu.com", "monetizer101.com", "royalads.net"
    )

    fun shouldBlock(url: String): Boolean {
        return blockList.any { url.contains(it) }
    }

    fun addToBlockList(domain: String) {
        // dynamic blocking via extension
    }
}
