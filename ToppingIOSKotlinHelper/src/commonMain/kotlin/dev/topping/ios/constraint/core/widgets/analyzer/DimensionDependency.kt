/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.topping.ios.constraint.core.widgets.analyzer

open class DimensionDependency(run: WidgetRun) : DependencyNode(run) {
    var wrapValue = 0

    init {
        if (run is HorizontalWidgetRun) {
            mType = Type.HORIZONTAL_DIMENSION
        } else {
            mType = Type.VERTICAL_DIMENSION
        }
    }

    
    override fun resolve(value: Int) {
        if (resolved) {
            return
        }
        this.resolved = true
        super.value = value
        for (node in mDependencies) {
            node.update(node)
        }
    }
}