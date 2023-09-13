package dev.topping.ios.constraint.shared.graphics

class ColorMatrixColorFilter {
    private val mMatrix = ColorMatrix()

    /**
     * Create a color filter that transforms colors through a 4x5 color matrix.
     *
     * @param matrix 4x5 matrix used to transform colors. It is copied into
     * the filter, so changes made to the matrix after the filter
     * is constructed will not be reflected in the filter.
     */
    constructor(matrix: ColorMatrix) {
        mMatrix.set(matrix)
    }

    /**
     * Create a color filter that transforms colors through a 4x5 color matrix.
     *
     * @param array Array of floats used to transform colors, treated as a 4x5
     * matrix. The first 20 entries of the array are copied into
     * the filter. See ColorMatrix.
     */
    constructor(array: FloatArray) {
        if (array.size < 20) {
            throw ArrayIndexOutOfBoundsException()
        }
        mMatrix.set(array)
    }

    /**
     * Copies the ColorMatrix from the filter into the passed ColorMatrix.
     *
     * @param colorMatrix Set to the current value of the filter's ColorMatrix.
     */
    fun getColorMatrix(colorMatrix: ColorMatrix) {
        colorMatrix.set(mMatrix)
    }

    /**
     * Copies the provided color matrix to be used by this filter.
     *
     * If the specified color matrix is null, this filter's color matrix will be reset to the
     * identity matrix.
     *
     * @param matrix A [ColorMatrix] or null
     *
     * @see .getColorMatrix
     * @see .setColorMatrixArray
     * @see ColorMatrix.reset
     * @hide
     */
    fun setColorMatrix(matrix: ColorMatrix?) {
        if (matrix == null) {
            mMatrix.reset()
        } else {
            mMatrix.set(matrix)
        }
    }
}