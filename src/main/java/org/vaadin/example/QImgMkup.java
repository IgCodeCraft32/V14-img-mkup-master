package org.vaadin.example;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.dependency.JsModule;

import com.vaadin.flow.component.page.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Usage TBD ...
 *
 */

@JsModule("js/script.js")

public class QImgMkup {

    private Page page = UI.getCurrent().getPage();
    private String base64Jpg = null;
    private EditCfg cfg = null;
    private Dimension dimension = null;
    private Div imgDiv = null;

    public enum Editor {
        VIEW_ONLY, // initial view - just show the image without any editing
        FREEFORM,
        CIRCLE,
        LINE,
        TEXT
    }

    public static class EditCfg {
        public String color = "red";
        public int thicknessPx = 4; // px
        public Editor editor = Editor.FREEFORM; // FREEFORM = default editor

        public String font = null; // a default font is probably OK
        public int font_size = 15; //
        public String txt = ""; //
    }

    public QImgMkup() {
    }

    public void setImage(String base64Img) {
        int w = 1;
        int h = 1;
        dimension = getImageDimension(base64Img);
        if (dimension != null) {
            w = dimension.width;
            h = dimension.height;
            if (imgDiv != null) {
                imgDiv.setWidth("" + w + "px");
                imgDiv.setHeight("" + h + "px");
            }
        }
        page.executeJs("ns.setImage($0, $1, $2)", base64Img, w, h);
    }

    public interface JpgReadyListener {
        public void jpgAsBase64Str(String s);
    }

    private final List<JpgReadyListener> listeners = new CopyOnWriteArrayList<>();

    private void registerJpgReadyCallback(JpgReadyListener c) {
        listeners.add(c);
    }

    private void unregisterJpgReadyCallback(JpgReadyListener c) {
        if (listeners.contains(c))
            listeners.remove(c);
    }

    public void registerForUpdatedImg(JpgReadyListener c) {
        registerJpgReadyCallback(c);
        page.executeJs("return ns.getEditedJpg()").then(result -> {
            String jpg = result.asString();
            for (JpgReadyListener listener : listeners) {
                listener.jpgAsBase64Str(jpg);
            }
            unregisterJpgReadyCallback(c);
        });
    }

    /** User clicked a button to choose an editor or line color/size. */
    public void setEditCfg(EditCfg cfg) {
        this.cfg = cfg;
        String col = cfg.color;
        int thick = cfg.thicknessPx;
        int font_size = cfg.font_size;
        page.executeJs("ns.setCfg($0,$1,$2)", col, thick, font_size);
    }

    public void addFreeform() {
        page.executeJs("ns.setTool($0)", "free");
    }

    public void addLiner() {
        page.executeJs("ns.setTool($0)", "line");
    }

    public void addCircler() {
        page.executeJs("ns.setTool($0)", "circle");
    }

    public void addText(String txt) {
        page.executeJs("ns.setTool($0, $1)", "text", txt);
    }

    /** User clicked UNDO button once. */
    public void undo() {
        page.executeJs("ns.undo()");
    }

    /** User clicked REDO button once. */
    public void redo() {
        page.executeJs("ns.redo()");
    }

    public Component getImgMkupEditor() {
        String canvas = "<canvas id='drawContainer' ></canvas>";
        imgDiv = new Div();
        if (dimension == null) {
            imgDiv.setWidth("1px");
            imgDiv.setHeight("1px");
        } else {
            imgDiv.setWidth("" + dimension.width + "px");
            imgDiv.setHeight("" + dimension.height + "px");
        }
        imgDiv.add(new Html(canvas));
        return imgDiv;
    }

    private Dimension getImageDimension(String base64Str) {
        if (base64Str == null)
            return null;
        if (base64Str.isEmpty())
            return null;
        if (base64Str.startsWith(MockDataSource.PREPEND_FOR_URL)) {
            base64Str = base64Str.substring(MockDataSource.PREPEND_FOR_URL.length());
        }
        Dimension d = null;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Str);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedBytes));
            d = new Dimension(image.getWidth(), image.getHeight());
        } catch (Throwable th) {
            System.out.println("th=" + th);
        }
        return d;
    }

}
