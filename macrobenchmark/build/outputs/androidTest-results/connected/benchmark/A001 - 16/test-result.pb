
∆,
[
StartupBenchmark"com.example.smartshuffle.benchmarkstartup2Ä∏π’¿®¿Ú:ô∏π’Ä µÓ•
øjava.lang.IllegalStateException: Unable to confirm activity launch completion [] Please report a bug with the output of `adb shell dumpsys gfxinfo com.example.smartshuffle framestats`
at androidx.benchmark.macro.MacrobenchmarkScope.startActivityImpl(MacrobenchmarkScope.kt:217)
at androidx.benchmark.macro.MacrobenchmarkScope.startActivityAndWait(MacrobenchmarkScope.kt:138)
at androidx.benchmark.macro.MacrobenchmarkScope.startActivityAndWait(MacrobenchmarkScope.kt:118)
at androidx.benchmark.macro.MacrobenchmarkScope.startActivityAndWait$default(MacrobenchmarkScope.kt:110)
at com.example.smartshuffle.benchmark.StartupBenchmark$startup$1.invoke(StartupBenchmark.kt:26)
at com.example.smartshuffle.benchmark.StartupBenchmark$startup$1.invoke(StartupBenchmark.kt:18)
at androidx.benchmark.macro.MacrobenchmarkKt$macrobenchmark$measurements$1$1$tracePath$1.invoke(Macrobenchmark.kt:286)
at androidx.benchmark.macro.MacrobenchmarkKt$macrobenchmark$measurements$1$1$tracePath$1.invoke(Macrobenchmark.kt:256)
at androidx.benchmark.perfetto.PerfettoCaptureWrapper.record(PerfettoCaptureWrapper.kt:142)
at androidx.benchmark.perfetto.PerfettoCaptureWrapper.record$default(PerfettoCaptureWrapper.kt:101)
at androidx.benchmark.macro.MacrobenchmarkKt$macrobenchmark$measurements$1.invoke(Macrobenchmark.kt:256)
at androidx.benchmark.macro.MacrobenchmarkKt$macrobenchmark$measurements$1.invoke(Macrobenchmark.kt:241)
at androidx.benchmark.perfetto.PerfettoTraceProcessor$Companion.runServer(PerfettoTraceProcessor.kt:105)
at androidx.benchmark.macro.MacrobenchmarkKt.macrobenchmark(Macrobenchmark.kt:241)
at androidx.benchmark.macro.MacrobenchmarkKt.macrobenchmarkWithStartupMode(Macrobenchmark.kt:422)
at androidx.benchmark.macro.junit4.MacrobenchmarkRule.measureRepeated(MacrobenchmarkRule.kt:107)
at androidx.benchmark.macro.junit4.MacrobenchmarkRule.measureRepeated$default(MacrobenchmarkRule.kt:97)
at com.example.smartshuffle.benchmark.StartupBenchmark.startup(StartupBenchmark.kt:18)
java.lang.IllegalStateExceptionøjava.lang.IllegalStateException: Unable to confirm activity launch completion [] Please report a bug with the output of `adb shell dumpsys gfxinfo com.example.smartshuffle framestats`
at androidx.benchmark.macro.MacrobenchmarkScope.startActivityImpl(MacrobenchmarkScope.kt:217)
at androidx.benchmark.macro.MacrobenchmarkScope.startActivityAndWait(MacrobenchmarkScope.kt:138)
at androidx.benchmark.macro.MacrobenchmarkScope.startActivityAndWait(MacrobenchmarkScope.kt:118)
at androidx.benchmark.macro.MacrobenchmarkScope.startActivityAndWait$default(MacrobenchmarkScope.kt:110)
at com.example.smartshuffle.benchmark.StartupBenchmark$startup$1.invoke(StartupBenchmark.kt:26)
at com.example.smartshuffle.benchmark.StartupBenchmark$startup$1.invoke(StartupBenchmark.kt:18)
at androidx.benchmark.macro.MacrobenchmarkKt$macrobenchmark$measurements$1$1$tracePath$1.invoke(Macrobenchmark.kt:286)
at androidx.benchmark.macro.MacrobenchmarkKt$macrobenchmark$measurements$1$1$tracePath$1.invoke(Macrobenchmark.kt:256)
at androidx.benchmark.perfetto.PerfettoCaptureWrapper.record(PerfettoCaptureWrapper.kt:142)
at androidx.benchmark.perfetto.PerfettoCaptureWrapper.record$default(PerfettoCaptureWrapper.kt:101)
at androidx.benchmark.macro.MacrobenchmarkKt$macrobenchmark$measurements$1.invoke(Macrobenchmark.kt:256)
at androidx.benchmark.macro.MacrobenchmarkKt$macrobenchmark$measurements$1.invoke(Macrobenchmark.kt:241)
at androidx.benchmark.perfetto.PerfettoTraceProcessor$Companion.runServer(PerfettoTraceProcessor.kt:105)
at androidx.benchmark.macro.MacrobenchmarkKt.macrobenchmark(Macrobenchmark.kt:241)
at androidx.benchmark.macro.MacrobenchmarkKt.macrobenchmarkWithStartupMode(Macrobenchmark.kt:422)
at androidx.benchmark.macro.junit4.MacrobenchmarkRule.measureRepeated(MacrobenchmarkRule.kt:107)
at androidx.benchmark.macro.junit4.MacrobenchmarkRule.measureRepeated$default(MacrobenchmarkRule.kt:97)
at com.example.smartshuffle.benchmark.StartupBenchmark.startup(StartupBenchmark.kt:18)
"Ω

logcatandroidß
§C:\MusicPLayer\macrobenchmark\build\outputs\androidTest-results\connected\benchmark\A001 - 16\logcat-com.example.smartshuffle.benchmark.StartupBenchmark-startup.txt"à

device-infoandroidn
lC:\MusicPLayer\macrobenchmark\build\outputs\androidTest-results\connected\benchmark\A001 - 16\device-info.pb"â

device-info.meminfoandroidg
eC:\MusicPLayer\macrobenchmark\build\outputs\androidTest-results\connected\benchmark\A001 - 16\meminfo"â

device-info.cpuinfoandroidg
eC:\MusicPLayer\macrobenchmark\build\outputs\androidTest-results\connected\benchmark\A001 - 16\cpuinfo*Ù
\additionalTestOutputFile_StartupBenchmark_startup_iter000_2026-09-19-09-55-37.perfetto-traceì/sdcard/Android/media/com.example.smartshuffle.benchmark/additional_test_output/StartupBenchmark_startup_iter000_2026-09-19-09-55-37.perfetto-trace*Ù
\additionalTestOutputFile_StartupBenchmark_startup_iter000_2026-09-19-09-55-37.perfetto-traceì/sdcard/Android/media/com.example.smartshuffle.benchmark/additional_test_output/StartupBenchmark_startup_iter000_2026-09-19-09-55-37.perfetto-trace*Ù
\additionalTestOutputFile_StartupBenchmark_startup_iter000_2026-09-19-09-55-37.perfetto-traceì/sdcard/Android/media/com.example.smartshuffle.benchmark/additional_test_output/StartupBenchmark_startup_iter000_2026-09-19-09-55-37.perfetto-trace*Ù
\additionalTestOutputFile_StartupBenchmark_startup_iter000_2026-09-19-09-55-37.perfetto-traceì/sdcard/Android/media/com.example.smartshuffle.benchmark/additional_test_output/StartupBenchmark_startup_iter000_2026-09-19-09-55-37.perfetto-trace*Ì
c
test-results.logOcom.google.testing.platform.runtime.android.driver.AndroidInstrumentationDriverx
vC:\MusicPLayer\macrobenchmark\build\outputs\androidTest-results\connected\benchmark\A001 - 16\testlog\test-results.log 2
text/plain