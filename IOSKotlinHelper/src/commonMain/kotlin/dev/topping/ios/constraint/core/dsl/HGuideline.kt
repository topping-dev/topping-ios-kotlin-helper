/*
 * Copyright (C) 2022 The Android Open Source Project
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
package dev.topping.ios.constraint.core.dsl

internal class HGuideline : Guideline {
    constructor(name: String) : super(name) {
        type = HelperType(typeMap.get(Type.HORIZONTAL_GUIDELINE)!!)
    }

    constructor(name: String, config: String) : super(name) {
        this.config = config
        type = HelperType(typeMap.get(Type.HORIZONTAL_GUIDELINE)!!)
        configMap = convertConfigToMap()
    }
}