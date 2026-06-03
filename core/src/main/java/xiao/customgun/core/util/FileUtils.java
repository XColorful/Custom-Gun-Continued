package xiao.customgun.core.util;

import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    // --------Functional Interfaces--------

    @FunctionalInterface
    public interface ReadFunction<T> {
        T apply(InputStream inputStream, Identifier fileLocation) throws IOException;
    }
    @FunctionalInterface
    public interface WriteFunction<T> {
        void accept(OutputStream outputStream) throws IOException;
    }
}
