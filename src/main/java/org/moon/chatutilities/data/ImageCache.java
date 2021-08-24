package org.moon.chatutilities.data;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.system.MemoryUtil;
import org.moon.chatutilities.ChatUtilities;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;

import static org.moon.chatutilities.data.ImageUtils.*;

public class ImageCache {

    private static final HashMap<String, ImageTexture> IMAGE_CACHE = new HashMap<>();

    public static void clear() {
        IMAGE_CACHE.clear();
    }

    public static ImageTexture getOrLoadImage(String imageURL) {
        if (!IMAGE_CACHE.containsKey(imageURL)) {
            Identifier texture_id = new Identifier("chatutils", ""+IMAGE_CACHE.size());

            // Create and register texture
            ImageTexture texture = new ImageTexture(imageURL);
            texture.identifier = texture_id;
            texture.registerTexture();

            // Add texture to cache
            IMAGE_CACHE.put(imageURL, texture);

            return texture;
        }

        return IMAGE_CACHE.get(imageURL);
    }

    public static class ImageTexture extends ResourceTexture {

        private static Identifier FALLBACK_ID = new Identifier("minecraft:textures/entity/steve.png");
        private static int MAX_WIDTH = 160;
        private static int MAX_HEIGHT = 90;

        public Identifier identifier;
        public byte[] data;
        public double ratio;
        public int width;
        public int height;

        public boolean ready = false;

        public ImageTexture(String url) {
            super(FALLBACK_ID);

            // Load image from thread
            new Thread(() -> {
                try {
                    // Get image from URL
                    URL imageUrl = new URL(url);
                    BufferedImage image = ImageIO.read(imageUrl);

                    // Scale down the image to save memory
                    if (image.getWidth() > MAX_WIDTH || image.getHeight() > MAX_HEIGHT)
                        image = resizeImage(image, width, height);
                    width = image.getWidth();
                    height = image.getHeight();
                    ratio = (double)width/height;

                    // Write the scaled image to data array
                    ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", byteOutStream);
                    data = byteOutStream.toByteArray();

                    // Upload texture using data array
                    uploadUsingData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        public void registerTexture() {
            ChatUtilities.getTextureManager().registerTexture(identifier, this);
        }

        public void uploadUsingData() {
            registerTexture();
            try {
                ByteBuffer wrapper = MemoryUtil.memAlloc(data.length);
                wrapper.put(data);
                wrapper.rewind();

                NativeImage nativeImage = NativeImage.read(wrapper);
                RenderSystem.recordRenderCall(() -> {
                    TextureUtil.prepareImage(getGlId(), width, height);
                    nativeImage.upload(0, 0, 0, true);
                    ready = true;
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}