package com.roadtonerdvana.trde;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;

import net.coobird.thumbnailator.Thumbnails;

/**
 * Hello world!
 *
 */
public class App {
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    class IconOrException {
        private Exception exception;
        private Icon icon;

        public IconOrException(Exception e) {
            this.exception = e;
        }

        public IconOrException(Icon icon) {
            this.icon = icon;
        }

        public Icon getIcon() throws Exception {
            if (icon == null) {
                throw exception;
            } else {
                return icon;
            }
        }
    }

    public IconOrException getIconOrException() throws Exception {

        Future<IconOrException> futureIcon = executor.submit(() -> {
            try {
                byte[] original = IOUtils.toByteArray(new URL("https://thispersondoesnotexist.com/image"));
                BufferedImage bi = ImageIO.read(new ByteArrayInputStream(original));
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    Thumbnails.of(bi).size(200, 200).outputFormat("png").toOutputStream(baos);
                    return new IconOrException(new ImageIcon(baos.toByteArray()));
                }
            } catch (Exception e) {
                return new IconOrException(e);
            }
        });

        return futureIcon.get(5000, TimeUnit.MILLISECONDS);

    }

    public void showDialog() {
        int option = JOptionPane.NO_OPTION;
        do {
            try {
                Icon icon = getIconOrException().getIcon();
                option = JOptionPane.showConfirmDialog(null, "another one?", "This Person Doesnt Exists",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Could not load image", "Error", JOptionPane.ERROR_MESSAGE);
                option = JOptionPane.NO_OPTION;
            }

        } while (option == JOptionPane.YES_OPTION);
        executor.shutdown();
    }

    public static void main(String[] args) throws MalformedURLException, IOException {
        new App().showDialog();

    }
}
