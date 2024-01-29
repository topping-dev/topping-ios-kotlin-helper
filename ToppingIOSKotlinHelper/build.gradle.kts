import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
}

kotlin {

    val xcf = XCFramework()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        /*it.compilations.getByName("main") {
            val AndroidUtils by cinterops.creating {
                headers(
                    "$projectDir/src/nativeInterop/cinterop/AndroidUtils.h",
                )
                extraOpts(
                    "-Xcompile-source",
                    "$projectDir/src/nativeInterop/cinterop/AndroidUtils.mm",
                    "-Xsource-compiler-option",
                    "-lobjc",
                    "-Xsource-compiler-option",
                    "-fobjc-arc",
                    "-Xsource-compiler-option",
                    "-DNS_FORMAT_ARGUMENT(A)=",
                )
            }
        }*/
        it.binaries.framework {
            baseName = "ToppingIOSKotlinHelper"
            embedBitcode(org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode.DISABLE)
            linkerOpts("-framework", "CoreText", "-framework", "Metal")
            xcf.add(this)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("io.github.pdvrieze.xmlutil:core:0.86.1")
                implementation("org.jetbrains.skiko:skiko:0.7.90")
                implementation("org.jetbrains.kotlinx:atomicfu:0.17.0")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}