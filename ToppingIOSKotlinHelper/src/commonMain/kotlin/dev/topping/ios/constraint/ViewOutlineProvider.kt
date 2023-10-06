package dev.topping.ios.constraint

import dev.topping.ios.constraint.shared.graphics.Outline

/**
 * Interface by which a TView builds its [Outline], used for shadow casting and clipping.
 */
abstract class ViewOutlineProvider {
    /**
     * Called to get the provider to populate the Outline.
     *
     * This method will be called by a TView when its owned Drawables are invalidated, when the
     * TView's size changes, or if [TView.invalidateOutline] is called
     * explicitly.
     *
     * The input outline is empty and has an alpha of `1.0f`.
     *
     * @param view The view building the outline.
     * @param outline The empty outline to be populated.
     */
    abstract fun getOutline(view: TView, outline: Outline)

    companion object {
        /**
         * Default outline provider for Views, which queries the Outline from the TView's background,
         * or generates a 0 alpha, rectangular Outline the size of the TView if a background
         * isn't present.
         *
         * @see Drawable.getOutline
         */
        val BACKGROUND: ViewOutlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: TView, outline: Outline) {
                val background = view.getBackground()
                if (background != null) {
                    background.getOutline(outline)
                } else {
                    outline.setRect(0, 0, view.getWidth(), view.getHeight())
                    outline.alpha = 0.0f
                }
            }
        }

        /**
         * Maintains the outline of the TView to match its rectangular bounds,
         * at `1.0f` alpha.
         *
         * This can be used to enable Views that are opaque but lacking a background cast a shadow.
         */
        val BOUNDS: ViewOutlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: TView, outline: Outline) {
                outline.setRect(0, 0, view.getWidth(), view.getHeight())
            }
        }

        /**
         * Maintains the outline of the TView to match its rectangular padded bounds,
         * at `1.0f` alpha.
         *
         * This can be used to enable Views that are opaque but lacking a background cast a shadow.
         */
        val PADDED_BOUNDS: ViewOutlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: TView, outline: Outline) {
                outline.setRect(
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getWidth() - view.getPaddingRight(),
                    view.getHeight() - view.getPaddingBottom()
                )
            }
        }
    }
}