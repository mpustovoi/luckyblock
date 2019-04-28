package mod.lucky.resources.loader;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import mod.lucky.Lucky;
import mod.lucky.resources.BaseResource;

public class DefaultLoader extends BaseLoader {
    private File resourceDir;

    public DefaultLoader(File minecraftDirectory) {
        this.resourceDir = new File(minecraftDirectory.getPath()
            + "/config/luckyBlock/version-" + Lucky.VERSION);

        this.setBlock(Lucky.luckyBlock);
        this.setSword(Lucky.luckySword);
        this.setBow(Lucky.luckyBow);
        this.setPotion(Lucky.luckyPotion);
    }

    public void extractDefaultResources() {
        try {
            InputStream stream = Lucky.class.getResourceAsStream("default_config.zip");
            ZipInputStream inputStream = new ZipInputStream(stream);

            ZipEntry entry;
            while ((entry = inputStream.getNextEntry()) != null) {
                FileOutputStream outputStream = null;
                File dest = new File(this.resourceDir.getPath() + "/" + entry.getName());
                if (!entry.isDirectory() && !dest.exists()) {
                    if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
                    dest.createNewFile();
                    outputStream = new FileOutputStream(dest);
                }

                int data;
                while ((data = inputStream.read()) != -1)
                    if (outputStream != null) outputStream.write(data);
                if (outputStream != null) outputStream.close();
            }
            inputStream.close();
        } catch (Exception e) {
            Lucky.LOGGER.error("Error extracting default resources");
        }
    }

    private File getFile(BaseResource resource) {
        return new File(this.resourceDir.getPath()
            + "/" + resource.getPath());
    }

    @Override
    public InputStream getResourceStream(BaseResource resource) {
        try {
            File file = this.getFile(resource);
            if (file.isDirectory()) return null;
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.err.println(
                "Lucky Block: Error getting default resource '" + resource.getPath() + "'");
            e.printStackTrace();
        }
        return null;
    }
}
