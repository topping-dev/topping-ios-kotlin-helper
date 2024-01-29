package dev.topping.ios.constraint

import nl.adaptivity.xmlutil.XmlBufferedReader
import nl.adaptivity.xmlutil.XmlStreaming


/**
 * A structure describing general information about a display, such as its
 * size, density, and font scaling.
 *
 * To access the TDisplayMetrics members, retrieve display metrics like this:
 * <pre>context.getResources().getDisplayMetrics();</pre>
 */
class TDisplayMetrics(deviceDensityParam: Int) {
    var deviceDensity = deviceDensityParam

    /**
     * The reference density used throughout the system.
     */
    val DENSITY_DEFAULT: Int = DENSITY_MEDIUM

    /**
     * Scaling factor to convert a density in DPI units to the density scale.
     * @hide
     */
    val DENSITY_DEFAULT_SCALE: Float = 1.0f / DENSITY_DEFAULT

    /**
     * The device's current density.
     *
     *
     * This value reflects any changes made to the device density. To obtain
     * the device's stable density, use [.DENSITY_DEVICE_STABLE].
     *
     * @hide This value should not be used.
     */
    var DENSITY_DEVICE: Int = deviceDensity

    /**
     * The device's stable density.
     *
     *
     * This value is constant at run time and may not reflect the current
     * display density. To obtain the current density for a specific display,
     * use [.densityDpi].
     */
    val DENSITY_DEVICE_STABLE: Int = deviceDensity

    // qemu.sf.lcd_density can be used to override ro.sf.lcd_density
    // when running in the emulator, allowing for dynamic configurations.
    // The reason for this is that ro.sf.lcd_density is write-once and is
    // set by the init process when it parses build.prop before anything else.

    /**
     * The absolute width of the available display size in pixels.
     */
    var widthPixels = 0

    /**
     * The absolute height of the available display size in pixels.
     */
    var heightPixels = 0

    /**
     * The logical density of the display.  This is a scaling factor for the
     * Density Independent Pixel unit, where one DIP is one pixel on an
     * approximately 160 dpi screen (for example a 240x320, 1.5"x2" screen),
     * providing the baseline of the system's display. Thus on a 160dpi screen
     * this density value will be 1; on a 120 dpi screen it would be .75; etc.
     *
     *
     * This value does not exactly follow the real screen size (as given by
     * [.xdpi] and [.ydpi]), but rather is used to scale the size of
     * the overall UI in steps based on gross changes in the display dpi.  For
     * example, a 240x320 screen will have a density of 1 even if its width is
     * 1.8", 1.3", etc. However, if the screen resolution is increased to
     * 320x480 but the screen size remained 1.5"x2" then the density would be
     * increased (probably to 1.5).
     *
     * @see .DENSITY_DEFAULT
     */
    var density = 0f

    /**
     * The screen density expressed as dots-per-inch.  May be either
     * [.DENSITY_LOW], [.DENSITY_MEDIUM], or [.DENSITY_HIGH].
     */
    var densityDpi = 0

    /**
     * A scaling factor for fonts displayed on the display.  This is the same
     * as [.density], except that it may be adjusted in smaller
     * increments at runtime based on a user preference for the font size.
     */
    var scaledDensity = 0f

    /**
     * The exact physical pixels per inch of the screen in the X dimension.
     */
    var xdpi = 0f

    /**
     * The exact physical pixels per inch of the screen in the Y dimension.
     */
    var ydpi = 0f

    /**
     * The reported display width prior to any compatibility mode scaling
     * being applied.
     * @hide
     */
    var noncompatWidthPixels = 0

    /**
     * The reported display height prior to any compatibility mode scaling
     * being applied.
     * @hide
     */
    var noncompatHeightPixels = 0

    /**
     * The reported display density prior to any compatibility mode scaling
     * being applied.
     * @hide
     */
    var noncompatDensity = 0f

    /**
     * The reported display density prior to any compatibility mode scaling
     * being applied.
     * @hide
     */
    var noncompatDensityDpi = 0

    /**
     * The reported scaled density prior to any compatibility mode scaling
     * being applied.
     * @hide
     */
    var noncompatScaledDensity = 0f

    /**
     * The reported display xdpi prior to any compatibility mode scaling
     * being applied.
     * @hide
     */
    var noncompatXdpi = 0f

    /**
     * The reported display ydpi prior to any compatibility mode scaling
     * being applied.
     * @hide
     */
    var noncompatYdpi = 0f

    init {
        setToDefaults()
    }

    fun setTo(o: TDisplayMetrics) {
        if (this === o) {
            return
        }
        widthPixels = o.widthPixels
        heightPixels = o.heightPixels
        density = o.density
        densityDpi = o.densityDpi
        scaledDensity = o.scaledDensity
        xdpi = o.xdpi
        ydpi = o.ydpi
        noncompatWidthPixels = o.noncompatWidthPixels
        noncompatHeightPixels = o.noncompatHeightPixels
        noncompatDensity = o.noncompatDensity
        noncompatDensityDpi = o.noncompatDensityDpi
        noncompatScaledDensity = o.noncompatScaledDensity
        noncompatXdpi = o.noncompatXdpi
        noncompatYdpi = o.noncompatYdpi
    }

    fun setToDefaults() {
        widthPixels = 0
        heightPixels = 0
        density =
            DENSITY_DEVICE / DENSITY_DEFAULT.toFloat()
        densityDpi = DENSITY_DEVICE
        scaledDensity = density
        xdpi = DENSITY_DEVICE.toFloat()
        ydpi = DENSITY_DEVICE.toFloat()
        noncompatWidthPixels = widthPixels
        noncompatHeightPixels = heightPixels
        noncompatDensity = density
        noncompatDensityDpi = densityDpi
        noncompatScaledDensity = scaledDensity
        noncompatXdpi = xdpi
        noncompatYdpi = ydpi
    }

    override fun equals(o: Any?): Boolean {
        return o is TDisplayMetrics && equals(o)
    }

    /**
     * Returns true if these display metrics equal the other display metrics.
     *
     * @param other The display metrics with which to compare.
     * @return True if the display metrics are equal.
     */
    fun equals(other: TDisplayMetrics): Boolean {
        return (equalsPhysical(other)
                && scaledDensity == other.scaledDensity && noncompatScaledDensity == other.noncompatScaledDensity)
    }

    /**
     * Returns true if the physical aspects of the two display metrics
     * are equal.  This ignores the scaled density, which is a logical
     * attribute based on the current desired font size.
     *
     * @param other The display metrics with which to compare.
     * @return True if the display metrics are equal.
     * @hide
     */
    fun equalsPhysical(other: TDisplayMetrics?): Boolean {
        return other != null && widthPixels == other.widthPixels && heightPixels == other.heightPixels && density == other.density && densityDpi == other.densityDpi && xdpi == other.xdpi && ydpi == other.ydpi && noncompatWidthPixels == other.noncompatWidthPixels && noncompatHeightPixels == other.noncompatHeightPixels && noncompatDensity == other.noncompatDensity && noncompatDensityDpi == other.noncompatDensityDpi && noncompatXdpi == other.noncompatXdpi && noncompatYdpi == other.noncompatYdpi
    }

    override fun hashCode(): Int {
        return widthPixels * heightPixels * densityDpi
    }

    override fun toString(): String {
        return "TDisplayMetrics{density=" + density + ", width=" + widthPixels +
                ", height=" + heightPixels + ", scaledDensity=" + scaledDensity +
                ", xdpi=" + xdpi + ", ydpi=" + ydpi + "}"
    }

    companion object {
        /**
         * Standard quantized DPI for low-density screens.
         */
        const val DENSITY_LOW = 120

        /**
         * Intermediate density for screens that sit between [.DENSITY_LOW] (120dpi) and
         * [.DENSITY_MEDIUM] (160dpi). This is not a density that applications should target,
         * instead relying on the system to scale their [.DENSITY_MEDIUM] assets for them.
         */
        const val DENSITY_140 = 140

        /**
         * Standard quantized DPI for medium-density screens.
         */
        const val DENSITY_MEDIUM = 160

        /**
         * Intermediate density for screens that sit between [.DENSITY_MEDIUM] (160dpi) and
         * [.DENSITY_HIGH] (240dpi). This is not a density that applications should target,
         * instead relying on the system to scale their [.DENSITY_HIGH] assets for them.
         */
        const val DENSITY_180 = 180

        /**
         * Intermediate density for screens that sit between [.DENSITY_MEDIUM] (160dpi) and
         * [.DENSITY_HIGH] (240dpi). This is not a density that applications should target,
         * instead relying on the system to scale their [.DENSITY_HIGH] assets for them.
         */
        const val DENSITY_200 = 200

        /**
         * This is a secondary density, added for some common screen configurations.
         * It is recommended that applications not generally target this as a first
         * class density -- that is, don't supply specific graphics for this
         * density, instead allow the platform to scale from other densities
         * (typically [.DENSITY_HIGH]) as
         * appropriate.  In most cases (such as using bitmaps in
         * [android.graphics.drawable.Drawable]) the platform
         * can perform this scaling at load time, so the only cost is some slight
         * startup runtime overhead.
         *
         *
         * This density was original introduced to correspond with a
         * 720p TV screen: the density for 1080p televisions is
         * [.DENSITY_XHIGH], and the value here provides the same UI
         * size for a TV running at 720p.  It has also found use in 7" tablets,
         * when these devices have 1280x720 displays.
         */
        const val DENSITY_TV = 213

        /**
         * Intermediate density for screens that sit between [.DENSITY_MEDIUM] (160dpi) and
         * [.DENSITY_HIGH] (240dpi). This is not a density that applications should target,
         * instead relying on the system to scale their [.DENSITY_HIGH] assets for them.
         */
        const val DENSITY_220 = 220

        /**
         * Standard quantized DPI for high-density screens.
         */
        const val DENSITY_HIGH = 240

        /**
         * Intermediate density for screens that sit between [.DENSITY_HIGH] (240dpi) and
         * [.DENSITY_XHIGH] (320dpi). This is not a density that applications should target,
         * instead relying on the system to scale their [.DENSITY_XHIGH] assets for them.
         */
        const val DENSITY_260 = 260

        /**
         * Intermediate density for screens that sit between [.DENSITY_HIGH] (240dpi) and
         * [.DENSITY_XHIGH] (320dpi). This is not a density that applications should target,
         * instead relying on the system to scale their [.DENSITY_XHIGH] assets for them.
         */
        const val DENSITY_280 = 280

        /**
         * Intermediate density for screens that sit between [.DENSITY_HIGH] (240dpi) and
         * [.DENSITY_XHIGH] (320dpi). This is not a density that applications should target,
         * instead relying on the system to scale their [.DENSITY_XHIGH] assets for them.
         */
        const val DENSITY_300 = 300

        /**
         * Standard quantized DPI for extra-high-density screens.
         */
        const val DENSITY_XHIGH = 320

        /**
         * Intermediate density for screens that sit somewhere between
         * [.DENSITY_XHIGH] (320 dpi) and [.DENSITY_XXHIGH] (480 dpi).
         * This is not a density that applications should target, instead relying
         * on the system to scale their [.DENSITY_XXHIGH] assets for them.
         */
        const val DENSITY_340 = 340

        /**
         * Intermediate density for screens that sit somewhere between
         * [.DENSITY_XHIGH] (320 dpi) and [.DENSITY_XXHIGH] (480 dpi).
         * This is not a density that applications should target, instead relying
         * on the system to scale their [.DENSITY_XXHIGH] assets for them.
         */
        const val DENSITY_360 = 360

        /**
         * Intermediate density for screens that sit somewhere between
         * [.DENSITY_XHIGH] (320 dpi) and [.DENSITY_XXHIGH] (480 dpi).
         * This is not a density that applications should target, instead relying
         * on the system to scale their [.DENSITY_XXHIGH] assets for them.
         */
        const val DENSITY_400 = 400

        /**
         * Intermediate density for screens that sit somewhere between
         * [.DENSITY_XHIGH] (320 dpi) and [.DENSITY_XXHIGH] (480 dpi).
         * This is not a density that applications should target, instead relying
         * on the system to scale their [.DENSITY_XXHIGH] assets for them.
         */
        const val DENSITY_420 = 420

        /**
         * Intermediate density for screens that sit somewhere between
         * [.DENSITY_XHIGH] (320 dpi) and [.DENSITY_XXHIGH] (480 dpi).
         * This is not a density that applications should target, instead relying
         * on the system to scale their [.DENSITY_XXHIGH] assets for them.
         */
        const val DENSITY_440 = 440

        /**
         * Intermediate density for screens that sit somewhere between
         * [.DENSITY_XHIGH] (320 dpi) and [.DENSITY_XXHIGH] (480 dpi).
         * This is not a density that applications should target, instead relying
         * on the system to scale their [.DENSITY_XXHIGH] assets for them.
         */
        const val DENSITY_450 = 450

        /**
         * Standard quantized DPI for extra-extra-high-density screens.
         */
        const val DENSITY_XXHIGH = 480

        /**
         * Intermediate density for screens that sit somewhere between
         * [.DENSITY_XXHIGH] (480 dpi) and [.DENSITY_XXXHIGH] (640 dpi).
         * This is not a density that applications should target, instead relying
         * on the system to scale their [.DENSITY_XXXHIGH] assets for them.
         */
        const val DENSITY_560 = 560

        /**
         * Intermediate density for screens that sit somewhere between
         * [.DENSITY_XXHIGH] (480 dpi) and [.DENSITY_XXXHIGH] (640 dpi).
         * This is not a density that applications should target, instead relying
         * on the system to scale their [.DENSITY_XXXHIGH] assets for them.
         */
        const val DENSITY_600 = 600

        /**
         * Standard quantized DPI for extra-extra-extra-high-density screens.  Applications
         * should not generally worry about this density; relying on XHIGH graphics
         * being scaled up to it should be sufficient for almost all cases.  A typical
         * use of this density would be 4K television screens -- 3840x2160, which
         * is 2x a traditional HD 1920x1080 screen which runs at DENSITY_XHIGH.
         */
        const val DENSITY_XXXHIGH = 640
    }
}