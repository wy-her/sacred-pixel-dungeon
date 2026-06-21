package com.sacredpixel.sacredpixeldungeon.teavm;

import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler;
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend;
import org.teavm.tooling.TeaVMSourceFilePolicy;
import org.teavm.vm.TeaVMOptimizationLevel;

import java.io.File;

/**
 * Build-time entry point for TeaVM compilation.
 * This class is run via Gradle JavaExec to compile the game to JavaScript.
 */
public class TeaVMBuilder {

    public static void main(String[] args) {
        boolean debug = false;
        boolean startJetty = false;
        String outputDir = "build/dist";  // default output directory

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("debug".equals(arg)) debug = true;
            else if ("run".equals(arg)) startJetty = true;
            else if ("--output-dir".equals(arg) && i + 1 < args.length) {
                outputDir = args[++i];
            }
        }

        WebBackend backend = new WebBackend()
                .setHtmlWidth(0)   // 0 = use all available space
                .setHtmlHeight(0)
                .setHtmlTitle("Sacred Pixel Dungeon")
                .setStartJettyAfterBuild(startJetty)
                .setJettyPort(8080);

        TeaCompiler compiler = new TeaCompiler(backend)
                .addAssets(new AssetFileHandle("../core/src/main/assets"))
                .setOptimizationLevel(debug
                        ? TeaVMOptimizationLevel.SIMPLE
                        : TeaVMOptimizationLevel.ADVANCED)
                .setMainClass(TeaVMLauncher.class.getName())
                .setObfuscated(false)
                .setDebugInformationGenerated(debug)
                .setSourceMapsFileGenerated(debug)
                .setSourceFilePolicy(debug
                        ? TeaVMSourceFilePolicy.COPY
                        : TeaVMSourceFilePolicy.DO_NOTHING);

        // Add source directories for source maps
        if (debug) {
            compiler.addSourceFileProvider(
                new org.teavm.tooling.sources.DirectorySourceFileProvider(
                    new File("../core/src/main/java/")));
            compiler.addSourceFileProvider(
                new org.teavm.tooling.sources.DirectorySourceFileProvider(
                    new File("../SPD-classes/src/main/java/")));
            compiler.addSourceFileProvider(
                new org.teavm.tooling.sources.DirectorySourceFileProvider(
                    new File("src/main/java/")));
        }

        compiler.build(new File(outputDir));

        // After TeaVM compiles (which generates a default index.html),
        // overwrite with our custom webapp files (index.html, styles.css, fonts).
        // This must happen AFTER build but BEFORE Jetty starts serving.
        copyWebappFiles(new File(outputDir, "webapp"));
    }

    private static void copyWebappFiles(File webappDir) {
        File srcDir = new File("webapp");
        if (!srcDir.exists()) return;

        try {
            // Copy index.html (overwrites TeaVM-generated default)
            copyFile(new File(srcDir, "index.html"), new File(webappDir, "index.html"));
            copyFile(new File(srcDir, "styles.css"), new File(webappDir, "styles.css"));
            copyFile(new File(srcDir, "banner.png"), new File(webappDir, "banner.png"));
            copyFile(new File(srcDir, "GRAC_Game_Grade_Text.png"), new File(webappDir, "GRAC_Game_Grade_Text.png"));
            copyFile(new File(srcDir, "og-image.png"), new File(webappDir, "og-image.png"));

            // Copy fonts directory
            File fontsDir = new File(srcDir, "fonts");
            File destFontsDir = new File(webappDir, "fonts");
            if (fontsDir.exists() && fontsDir.isDirectory()) {
                destFontsDir.mkdirs();
                for (File f : fontsDir.listFiles()) {
                    if (f.isFile()) {
                        copyFile(f, new File(destFontsDir, f.getName()));
                    }
                }
            }

            // Disable music preloading (large files)
            File preloadFile = new File(webappDir, "assets/preload.txt");
            if (preloadFile.exists()) {
                String content = new String(java.nio.file.Files.readAllBytes(preloadFile.toPath()));
                content = content.replaceAll("(i:b:music/[^:]+:[^:]+):1", "$1:0");
                java.nio.file.Files.write(preloadFile.toPath(), content.getBytes());
                System.out.println("Disabled preloading for music files in preload.txt");
            }
        } catch (Exception e) {
            System.err.println("Warning: failed to copy webapp files: " + e.getMessage());
        }
    }

    private static void copyFile(File src, File dest) throws java.io.IOException {
        if (!src.exists()) return;
        java.nio.file.Files.copy(src.toPath(), dest.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}
