package mod.lucky.world;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import mod.lucky.Lucky;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LuckyTickHandler {
    private ConcurrentHashMap<Integer, Object> delayDrops;
    private boolean alreadyShownMessage = false;
    private static boolean showUpdateMessage = true;

    public LuckyTickHandler() {
        try {
            this.delayDrops = new ConcurrentHashMap<Integer, Object>();
        } catch (Exception e) {}
    }

    public static void setShowUpdateMessage(boolean showUpdateMessage) {
        LuckyTickHandler.showUpdateMessage = showUpdateMessage;
    }

    public static String versionLog = "" +
      "" +
        "7.8.0|1.14.0|[\"\",{\"text\":\"Lucky Block > \",\"color\":\"gold\"},{\"text\":\"A new version is available for Minecraft 1.14. You can download it \",\"color\":\"gold\"},{\"text\":\"here!\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://www.minecraftascending.com/projects/lucky_block/lucky_block.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Download Lucky Block\",\"color\":\"blue\"}]}}}]\r\n" +
      "" +
      "7.5.0|1.12.0|[\"\",{\"text\":\"Lucky Block > \",\"color\":\"gold\"},{\"text\":\"A new version is available for Minecraft 1.12. You can download it \",\"color\":\"gold\"},{\"text\":\"here!\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://www.minecraftascending.com/projects/lucky_block/lucky_block.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Download Lucky Block\",\"color\":\"blue\"}]}}}]\r\n" +
        "" +
        "" +
        "" +
        "7.3.0|1.10.2|[\"\",{\"text\":\"Lucky Block > \",\"color\":\"gold\"},{\"text\":\"A new version is available for Minecraft 1.10.2. You can download it \",\"color\":\"gold\"},{\"text\":\"here!\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://www.minecraftascending.com/projects/lucky_block/lucky_block.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Download Lucky Block\",\"color\":\"blue\"}]}}}]\r\n7.0.2|1.8.0|[\"\",{\"text\":\"Lucky Block > \",\"color\":\"gold\"},{\"text\":\"A new version is available for Minecraft 1.8.9 and 1.9. You can download it \",\"color\":\"gold\"},{\"text\":\"here!\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://www.minecraftascending.com/projects/lucky_block/lucky_block.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Download Lucky Block\",\"color\":\"blue\"}]}}}]\r\n7.0.1|1.8.0|[\"\",{\"text\":\"Lucky Block > \",\"color\":\"gold\"},{\"text\":\"A new version fixes several issues. You can download it \",\"color\":\"gold\"},{\"text\":\"here!\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://www.minecraftascending.com/projects/lucky_block/lucky_block.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Download Lucky Block\",\"color\":\"blue\"}]}}}]";

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        try {
            if (LuckyTickHandler.showUpdateMessage
                && Minecraft.getInstance().player != null
                && !this.alreadyShownMessage) {

                this.alreadyShownMessage = true;

                URL url = new URL("http://www.minecraftascending.com/projects/lucky_block/download/version/version_log.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                reader = new BufferedReader(new StringReader(versionLog));

                int curLuckyVersion = Integer.valueOf(Lucky.VERSION.replace(".", ""));
                int curMinecraftVersion = Integer.valueOf(Lucky.MC_VERSION.replace(".", ""));

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split("\\|");
                    int luckyVersion = Integer.valueOf(split[0].replace(".", ""));
                    int minecraftVersion = Integer.valueOf(split[1].replace(".", ""));

                    Lucky.LOGGER.info(luckyVersion + ", " + minecraftVersion);
                    Lucky.LOGGER.info("> " + curLuckyVersion + ", " + curMinecraftVersion);

                    if (minecraftVersion >= curMinecraftVersion && luckyVersion > curLuckyVersion) {
                        String message = split[2];
                        ITextComponent textComponent = ITextComponent.Serializer.fromJson(message);
                        Lucky.LOGGER.info("ATTEMPT MESSAGE: " + textComponent);
                        Minecraft.getInstance().player.sendMessage(textComponent);
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onServerTick(TickEvent.ServerTickEvent event) {
        try {
            for (int i = 0; i > -1; i++) {
                if (this.delayDrops.containsKey(i) && this.delayDrops.get(i) instanceof DelayLuckyDrop) {
                    DelayLuckyDrop delayDrop = (DelayLuckyDrop) this.delayDrops.get(i);
                    delayDrop.update();
                    if (delayDrop.finished()) {
                        this.delayDrops.remove(i);
                        if (this.delayDrops.containsKey(i + 1)) this.delayDrops.put(i, 0);
                    }
                }
                if (!this.delayDrops.containsKey(i)) break;
            }
        } catch (Exception e) {
            Lucky.error(e, "Error processing delay drop");
            this.delayDrops.clear();
        }
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save event) {
        try {
            if (this.delayDrops.size() > 0) {
                boolean saved = false;
                NBTTagList dropTags = new NBTTagList();

                for (int i = 0; i > -1; i++) {
                    if (this.delayDrops.containsKey(i)
                    && this.delayDrops.get(i) instanceof DelayLuckyDrop) {
                        DelayLuckyDrop delayDrop = (DelayLuckyDrop) this.delayDrops.get(i);

                        BlockPos harvestPos = delayDrop.getProcessData().getHarvestBlockPos();
                        ChunkPos harvestChunkPos = event.getChunk().getWorldForge()
                            .getChunkDefault(harvestPos).getPos();

                        if (harvestChunkPos == event.getChunk().getPos()) {
                            dropTags.add(delayDrop.writeToNBT());
                            saved = true;
                        }
                    }
                    if (!this.delayDrops.containsKey(i)) break;
                }

                if (saved) event.getData().setTag("LuckyBlockDelayDrops", dropTags);
            }
        } catch (Exception e) {
            Lucky.error(e, "Error saving chunk properties");
            this.delayDrops.clear();
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent.Load event) {
        try {
            if (event.getData().hasKey("LuckyBlockDelayDrops")) {
                NBTTagList delayDropTags = event.getData().getList("LuckyBlockDelayDrops", 10);
                for (int i = 0; i < delayDropTags.size(); i++) {
                    DelayLuckyDrop delayDrop =
                        new DelayLuckyDrop(Lucky.luckyBlock.getDropProcessor(), null, 0);
                    delayDrop.readFromNBT(delayDropTags.getCompound(i),
                        event.getChunk().getWorldForge().getWorld());
                    this.addDelayDrop(delayDrop);
                }
            }
        } catch (Exception e) {
            Lucky.error(e, "Error loading chunk properties");
            this.delayDrops.clear();
        }
    }

    public void addDelayDrop(DelayLuckyDrop delayDrop) {
        for (int i = 0; i > -1; i++) {
            if (!this.delayDrops.containsKey(i)
                || !(this.delayDrops.get(i) instanceof DelayLuckyDrop)) {

                this.delayDrops.put(i, delayDrop);
                break;
            }
        }
    }

    public void addDelayDrop(DropProcessor dropProcessor, DropProcessData processData, float delay) {
        this.addDelayDrop(new DelayLuckyDrop(dropProcessor, processData, (long) (delay * 20)));
    }
}
