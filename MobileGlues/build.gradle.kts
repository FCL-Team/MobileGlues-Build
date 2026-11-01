// 与 AngelAuraMC/amethyst-prebuilt-libraries renderers/MobileGlues 保持一致：
// 以 MobileGL-Dev/MobileGlues（dev 分支）为源码，叠加许可证资源目录。
// 上游 dev 分支若调整构建配置（ndkVersion / compileSdk 等），需同步本文件。
plugins {
    id("com.android.library")
}

android {
    namespace = "top.mobilegl.mobileglues"
    compileSdk = 36

    defaultConfig {
        minSdk = 21

        ndkVersion = "27.3.13750724"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
        create("proguard") {
            isMinifyEnabled = true
            initWith(getByName("debug"))
        }
        create("fordebug") {
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("assets")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("MobileGlues/MobileGlues-cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
