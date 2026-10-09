package pl.s16.dungeons;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Najpierw zapis do nowego pliku, potem atomowa podmiana tam, gdzie filesystem to obsluguje. */
final class AtomicYaml {
    private AtomicYaml() {}
    static void write(YamlConfiguration data, File file) throws IOException {
        Path dest=file.toPath();Files.createDirectories(dest.getParent());
        Path tmp=Files.createTempFile(dest.getParent(),file.getName(),".tmp");
        try {
            Files.writeString(tmp,data.saveToString(),StandardCharsets.UTF_8);
            try {Files.move(tmp,dest,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(AtomicMoveNotSupportedException e) {Files.move(tmp,dest,StandardCopyOption.REPLACE_EXISTING);}
        } finally {Files.deleteIfExists(tmp);}
    }
}
