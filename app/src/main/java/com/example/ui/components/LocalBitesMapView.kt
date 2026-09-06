package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.entity.Vendor
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CharcoalBackground
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LocalBitesMapView(
    vendors: List<Vendor>,
    selectedVendorId: Long?,
    userLatitude: Double?,
    userLongitude: Double?,
    onVendorSelected: (Vendor) -> Unit,
    modifier: Modifier = Modifier,
    isPickerMode: Boolean = false,
    pickerLat: Double = userLatitude ?: 24.8615,
    pickerLng: Double = userLongitude ?: 67.0099,
    onPickerLocationChanged: ((Double, Double) -> Unit)? = null
) {
    var isMapLoaded by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Map web bridge
    val jsBridge = remember(vendors, onVendorSelected, onPickerLocationChanged) {
        object {
            @JavascriptInterface
            fun onVendorClick(idStr: String) {
                val id = idStr.toLongOrNull() ?: return
                val vendor = vendors.find { it.id == id } ?: return
                onVendorSelected(vendor)
            }

            @JavascriptInterface
            fun onLocationPicked(lat: Double, lng: Double) {
                onPickerLocationChanged?.invoke(lat, lng)
            }

            @JavascriptInterface
            fun onMapReady() {
                isMapLoaded = true
            }
        }
    }

    Box(modifier = modifier.background(CharcoalBackground)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    // Ensure cache directory structure exists to avoid chromium cache initialization errors
                    try {
                        val cacheDir = java.io.File(context.cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
                        if (!cacheDir.exists()) {
                            cacheDir.mkdirs()
                        }
                    } catch (_: Exception) {}

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    // In virtual cloud emulator environments, software layer avoids querying non-existent DRM render nodes
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    setBackgroundColor(android.graphics.Color.parseColor("#1A1614"))
                    webChromeClient = object : WebChromeClient() {
    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage): Boolean {
        android.util.Log.d("MapWebView", "${consoleMessage.message()} -- From line ${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}")
        return true
    }
}
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isMapLoaded = true
                        }
                    }
                    addJavascriptInterface(jsBridge, "LocalBitesBridge")
                    loadDataWithBaseURL(
                        "https://localbites.app",
                        generateMapHtml(
                            isPickerMode = isPickerMode,
                            initialLat = if (isPickerMode) pickerLat else (userLatitude ?: 24.8615),
                            initialLng = if (isPickerMode) pickerLng else (userLongitude ?: 67.0099)
                        ),
                        "text/html",
                        "UTF-8",
                        null
                    )
                    webViewRef = this
                }
            },
            update = { webView ->
                webViewRef = webView
                if (isMapLoaded) {
                    if (!isPickerMode) {
                        updateVendorsOnMap(webView, vendors, selectedVendorId, userLatitude, userLongitude)
                    } else {
                        updatePickerPin(webView, pickerLat, pickerLng)
                    }
                }
            }
        )

        // When data updates, reflect on map
        LaunchedEffect(vendors, selectedVendorId, userLatitude, userLongitude, isMapLoaded) {
            if (isMapLoaded && !isPickerMode) {
                webViewRef?.let {
                    updateVendorsOnMap(it, vendors, selectedVendorId, userLatitude, userLongitude)
                }
            }
        }

        if (!isMapLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CharcoalBackground.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AmberPrimary,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

private fun updateVendorsOnMap(
    webView: WebView,
    vendors: List<Vendor>,
    selectedVendorId: Long?,
    userLat: Double?,
    userLng: Double?
) {
    val vendorsArray = JSONArray()
    vendors.forEach { vendor ->
        val obj = JSONObject().apply {
            put("id", vendor.id.toString())
            put("name", vendor.name)
            put("category", vendor.category)
            put("price", vendor.priceRange)
            put("rating", vendor.avgRating)
            put("isOpen", vendor.isOpen)
            put("lat", vendor.latitude)
            put("lng", vendor.longitude)
            put("isSelected", vendor.id == selectedVendorId)
        }
        vendorsArray.put(obj)
    }

    val userObj = if (userLat != null && userLng != null) {
        JSONObject().apply {
            put("lat", userLat)
            put("lng", userLng)
        }.toString()
    } else "null"

    val script = "if (window.renderVendors) { window.renderVendors($vendorsArray, $userObj, ${selectedVendorId ?: "null"}); }"
    webView.post {
        webView.evaluateJavascript(script, null)
    }
}

private fun updatePickerPin(webView: WebView, lat: Double, lng: Double) {
    val script = "if (window.setPickerPosition) { window.setPickerPosition($lat, $lng); }"
    webView.post {
        webView.evaluateJavascript(script, null)
    }
}

private fun generateMapHtml(
    isPickerMode: Boolean,
    initialLat: Double,
    initialLng: Double
): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                html, body, #map {
                    width: 100%;
                    height: 100%;
                    background: #1A1614;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    overflow: hidden;
                }
                /* Dark Charcoal Map Filter */
                .leaflet-tile {
                    filter: brightness(0.65) invert(1) contrast(3) hue-rotate(180deg) saturate(0.35);
                }
                .leaflet-container {
                    background: #1A1614 !important;
                }
                /* Custom Amber Vendor Pin */
                .amber-vendor-pin {
                    position: relative;
                    width: 38px;
                    height: 46px;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    transition: transform 0.2s ease;
                }
                .amber-vendor-pin:hover, .amber-vendor-pin.selected {
                    transform: scale(1.18);
                    z-index: 1000 !important;
                }
                .amber-pin-badge {
                    width: 36px;
                    height: 36px;
                    background: #241E1B;
                    border: 2.5px solid #E8770E;
                    border-radius: 50% 50% 50% 0;
                    transform: rotate(-45deg);
                    box-shadow: 0 4px 10px rgba(0,0,0,0.5);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
                .amber-pin-content {
                    transform: rotate(45deg);
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                }
                .pin-icon {
                    font-size: 14px;
                    line-height: 1;
                }
                .pin-status-dot {
                    position: absolute;
                    bottom: -2px;
                    right: -2px;
                    width: 8px;
                    height: 8px;
                    border-radius: 50%;
                    border: 1.5px solid #1A1614;
                }
                .status-open { background: #22C55E; }
                .status-closed { background: #EF4444; }

                /* User GPS Pulse */
                .user-gps-pulse {
                    position: relative;
                    width: 22px;
                    height: 22px;
                }
                .pulse-center {
                    width: 14px;
                    height: 14px;
                    background: #3B82F6;
                    border: 2px solid #FFFFFF;
                    border-radius: 50%;
                    position: absolute;
                    top: 4px;
                    left: 4px;
                    box-shadow: 0 0 8px rgba(59, 130, 246, 0.8);
                }
                .pulse-ring {
                    width: 22px;
                    height: 22px;
                    border-radius: 50%;
                    background: rgba(59, 130, 246, 0.35);
                    animation: pulse-ring 1.8s infinite cubic-bezier(0.2, 0.6, 0.3, 1);
                }
                @keyframes pulse-ring {
                    0% { transform: scale(0.6); opacity: 1; }
                    100% { transform: scale(2.2); opacity: 0; }
                }

                /* Picker Pin */
                .picker-pin {
                    width: 44px;
                    height: 52px;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                }
                .picker-bubble {
                    background: #E8770E;
                    color: #FFFFFF;
                    font-weight: 700;
                    font-size: 11px;
                    padding: 3px 8px;
                    border-radius: 12px;
                    box-shadow: 0 3px 8px rgba(0,0,0,0.6);
                    white-space: nowrap;
                    margin-bottom: 2px;
                }
                .leaflet-bar a {
                    background-color: #241E1B !important;
                    color: #F59E0B !important;
                    border-bottom: 1px solid #3D322B !important;
                }
                .leaflet-control-attribution {
                    background: rgba(26, 22, 20, 0.8) !important;
                    color: #7A6F68 !important;
                    font-size: 9px !important;
                }
                .leaflet-control-attribution a {
                    color: #E8770E !important;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {
                    zoomControl: false,
                    attributionControl: false
                }).setView([$initialLat, $initialLng], 15);

                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19
                }).addTo(map);

                var vendorMarkers = {};
                var userMarker = null;
                var pickerMarker = null;
                var isPicker = $isPickerMode;

                function getCategoryIcon(cat) {
                    if (cat === 'Chai') return '☕';
                    if (cat === 'Fast Food') return '🍔';
                    if (cat === 'Snacks') return '🥟';
                    if (cat === 'Juice') return '🥤';
                    if (cat === 'Desi Food') return '🥘';
                    return '🍲';
                }

                if (isPicker) {
                    var pickerIcon = L.divIcon({
                        className: 'custom-div-icon',
                        html: '<div class="picker-pin"><div class="picker-bubble">Vendor Spot</div><div class="amber-pin-badge" style="background:#E8770E;border-color:#FFF;"><div class="amber-pin-content"><span class="pin-icon">📍</span></div></div></div>',
                        iconSize: [44, 52],
                        iconAnchor: [22, 50]
                    });

                    pickerMarker = L.marker([$initialLat, $initialLng], {
                        draggable: true,
                        icon: pickerIcon
                    }).addTo(map);

                    pickerMarker.on('dragend', function(e) {
                        var pos = pickerMarker.getLatLng();
                        if (window.LocalBitesBridge && window.LocalBitesBridge.onLocationPicked) {
                            window.LocalBitesBridge.onLocationPicked(pos.lat, pos.lng);
                        }
                    });

                    map.on('click', function(e) {
                        pickerMarker.setLatLng(e.latlng);
                        if (window.LocalBitesBridge && window.LocalBitesBridge.onLocationPicked) {
                            window.LocalBitesBridge.onLocationPicked(e.latlng.lat, e.latlng.lng);
                        }
                    });

                    window.setPickerPosition = function(lat, lng) {
                        if (pickerMarker) {
                            pickerMarker.setLatLng([lat, lng]);
                            map.panTo([lat, lng]);
                        }
                    };
                }

                window.renderVendors = function(vendors, userPos, selectedId) {
                    if (isPicker) return;

                    // Clear removed markers
                    var currentIds = {};
                    vendors.forEach(function(v) { currentIds[v.id] = true; });
                    for (var id in vendorMarkers) {
                        if (!currentIds[id]) {
                            map.removeLayer(vendorMarkers[id]);
                            delete vendorMarkers[id];
                        }
                    }

                    // Add/update markers
                    vendors.forEach(function(vendor) {
                        var isSel = (vendor.id == selectedId);
                        var statusClass = vendor.isOpen ? 'status-open' : 'status-closed';
                        var catEmoji = getCategoryIcon(vendor.category);

                        var html = '<div class="amber-vendor-pin ' + (isSel ? 'selected' : '') + '">' +
                            '<div class="amber-pin-badge">' +
                                '<div class="amber-pin-content">' +
                                    '<span class="pin-icon">' + catEmoji + '</span>' +
                                '</div>' +
                            '</div>' +
                            '<div class="pin-status-dot ' + statusClass + '"></div>' +
                        '</div>';

                        var icon = L.divIcon({
                            className: 'custom-vendor-marker',
                            html: html,
                            iconSize: [38, 46],
                            iconAnchor: [19, 44]
                        });

                        if (vendorMarkers[vendor.id]) {
                            vendorMarkers[vendor.id].setLatLng([vendor.lat, vendor.lng]);
                            vendorMarkers[vendor.id].setIcon(icon);
                        } else {
                            var m = L.marker([vendor.lat, vendor.lng], { icon: icon }).addTo(map);
                            m.on('click', function() {
                                if (window.LocalBitesBridge && window.LocalBitesBridge.onVendorClick) {
                                    window.LocalBitesBridge.onVendorClick(vendor.id);
                                }
                            });
                            vendorMarkers[vendor.id] = m;
                        }
                    });

                    // User GPS marker
                    if (userPos && userPos.lat && userPos.lng) {
                        var userIcon = L.divIcon({
                            className: 'custom-user-marker',
                            html: '<div class="user-gps-pulse"><div class="pulse-ring"></div><div class="pulse-center"></div></div>',
                            iconSize: [22, 22],
                            iconAnchor: [11, 11]
                        });

                        if (!userMarker) {
                            userMarker = L.marker([userPos.lat, userPos.lng], { icon: userIcon }).addTo(map);
                        } else {
                            userMarker.setLatLng([userPos.lat, userPos.lng]);
                        }
                    }

                    if (selectedId && vendorMarkers[selectedId]) {
                    map.panTo(vendorMarkers[selectedId].getLatLng(), { animate: true });
                    } else if (userPos && userPos.lat && userPos.lng) {
                    map.panTo([userPos.lat, userPos.lng], { animate: true });
                }
                };

                if (window.LocalBitesBridge && window.LocalBitesBridge.onMapReady) {
                    window.LocalBitesBridge.onMapReady();
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}
