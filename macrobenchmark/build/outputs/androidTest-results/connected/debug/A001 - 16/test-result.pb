
¯
[
StartupBenchmark"com.example.smartshuffle.benchmarkstartup2ñ­¹ÕÀÔëÆ:ñ­¹Õ€áý‹ú
­java.lang.AssertionError: ERRORS (not suppressed): DEBUGGABLE
WARNINGS (suppressed):

ERROR: Benchmark Target is Debuggable
Target package com.example.smartshuffle is running with debuggable=true in its manifest,
which drastically reduces runtime performance in order to support debugging
features. Run benchmarks with debuggable=false. Debuggable affects execution
speed in ways that mean benchmark improvements might not carry over to a
real user's experience (or even regress release performance).

While you can suppress these errors (turning them into warnings)
PLEASE NOTE THAT EACH SUPPRESSED ERROR COMPROMISES ACCURACY

// Sample suppression, in a benchmark module's build.gradle:
android {
defaultConfig {
testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "DEBUGGABLE"
}
}
at androidx.benchmark.ConfigurationErrorKt.checkAndGetSuppressionState(ConfigurationError.kt:123)
at androidx.benchmark.macro.MacrobenchmarkKt.checkErrors(Macrobenchmark.kt:172)
at androidx.benchmark.macro.MacrobenchmarkKt.macrobenchmark(Macrobenchmark.kt:203)
at androidx.benchmark.macro.MacrobenchmarkKt.macrobenchmarkWithStartupMode(Macrobenchmark.kt:422)
at androidx.benchmark.macro.junit4.MacrobenchmarkRule.measureRepeated(MacrobenchmarkRule.kt:107)
at androidx.benchmark.macro.junit4.MacrobenchmarkRule.measureRepeated$default(MacrobenchmarkRule.kt:97)
at com.example.smartshuffle.benchmark.StartupBenchmark.startup(StartupBenchmark.kt:18)
java.lang.AssertionError­java.lang.AssertionError: ERRORS (not suppressed): DEBUGGABLE
WARNINGS (suppressed):

ERROR: Benchmark Target is Debuggable
Target package com.example.smartshuffle is running with debuggable=true in its manifest,
which drastically reduces runtime performance in order to support debugging
features. Run benchmarks with debuggable=false. Debuggable affects execution
speed in ways that mean benchmark improvements might not carry over to a
real user's experience (or even regress release performance).

While you can suppress these errors (turning them into warnings)
PLEASE NOTE THAT EACH SUPPRESSED ERROR COMPROMISES ACCURACY

// Sample suppression, in a benchmark module's build.gradle:
android {
defaultConfig {
testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "DEBUGGABLE"
}
}
at androidx.benchmark.ConfigurationErrorKt.checkAndGetSuppressionState(ConfigurationError.kt:123)
at androidx.benchmark.macro.MacrobenchmarkKt.checkErrors(Macrobenchmark.kt:172)
at androidx.benchmark.macro.MacrobenchmarkKt.macrobenchmark(Macrobenchmark.kt:203)
at androidx.benchmark.macro.MacrobenchmarkKt.macrobenchmarkWithStartupMode(Macrobenchmark.kt:422)
at androidx.benchmark.macro.junit4.MacrobenchmarkRule.measureRepeated(MacrobenchmarkRule.kt:107)
at androidx.benchmark.macro.junit4.MacrobenchmarkRule.measureRepeated$default(MacrobenchmarkRule.kt:97)
at com.example.smartshuffle.benchmark.StartupBenchmark.startup(StartupBenchmark.kt:18)
"¹

logcatandroid£
 C:\MusicPLayer\macrobenchmark\build\outputs\androidTest-results\connected\debug\A001 - 16\logcat-com.example.smartshuffle.benchmark.StartupBenchmark-startup.txt"„

device-infoandroidj
hC:\MusicPLayer\macrobenchmark\build\outputs\androidTest-results\connected\debug\A001 - 16\device-info.pb"…

device-info.meminfoandroidc
aC:\MusicPLayer\macrobenchmark\build\outputs\androidTest-results\connected\debug\A001 - 16\meminfo"…

device-info.cpuinfoandroidc
aC:\MusicPLayer\macrobenchmark\build\outputs\androidTest-results\connected\debug\A001 - 16\cpuinfo*é
c
test-results.logOcom.google.testing.platform.runtime.android.driver.AndroidInstrumentationDrivert
rC:\MusicPLayer\macrobenchmark\build\outputs\androidTest-results\connected\debug\A001 - 16\testlog\test-results.log 2
text/plain