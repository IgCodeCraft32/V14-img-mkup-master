package org.vaadin.example;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

  private VerticalLayout workSpace = null;
  private QImgMkup qimgMkup = null;
  private QImgMkup.EditCfg cfg = null;
  private Span editorTitle = null;
  private String textValue = "";
  private ColorRange currentColor = null;

  public MainView() {
    this.setSizeFull();
    add(new H2("Trialing Image Markup"));

    cfg = new QImgMkup.EditCfg();
    cfg.color = ColorRange.RED.colStr;
    cfg.thicknessPx = 8;
    cfg.editor = QImgMkup.Editor.FREEFORM;
    cfg.font_size = 30;

    HorizontalLayout trialToolBar = new HorizontalLayout();
    trialToolBar.setAlignItems(Alignment.BASELINE);
    add(trialToolBar);
    trialToolBar.add(new Label("Trial Controls:"));
    Button startEditingButton = new Button("Load/Reload");
    startEditingButton.setIcon(VaadinIcon.PLAY.create());
    startEditingButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS,
        ButtonVariant.LUMO_SMALL);
    trialToolBar.add(startEditingButton);
    startEditingButton.addClickListener(clk -> {
      startEditing();
    });

    Button launchViewerButton = new Button("View marked up img");
    launchViewerButton.setIcon(VaadinIcon.CAMERA.create());
    launchViewerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS,
        ButtonVariant.LUMO_SMALL);
    trialToolBar.add(launchViewerButton);
    launchViewerButton.addClickListener(clk -> {
      finishEditing();
    });

    Button undoButton = new Button("Undo");
    undoButton.setIcon(VaadinIcon.ARROW_BACKWARD.create());
    undoButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
    trialToolBar.add(undoButton);
    undoButton.addClickListener(clk -> {
      qimgMkup.undo();
    });

    Button redoButton = new Button("Redo");
    redoButton.setIcon(VaadinIcon.ARROW_FORWARD.create());
    redoButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
    trialToolBar.add(redoButton);
    redoButton.addClickListener(clk -> {
      qimgMkup.redo();
    });

    HorizontalLayout imgToolBar = new HorizontalLayout();
    imgToolBar.setAlignItems(Alignment.BASELINE);
    add(imgToolBar);
    imgToolBar.add(new Label("Tools:"));

    imgToolBar.add(createThicknessPicker());
    imgToolBar.add(createColorPicker());

    Button launchLineButton = new Button("Line");
    launchLineButton.setIcon(VaadinIcon.LINE_V.create());
    launchLineButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
    launchLineButton.addClickListener(clk -> {
      launchLineEditor();
    });
    imgToolBar.add(launchLineButton);

    Button launchCircleButton = new Button("Circle");
    launchCircleButton.setIcon(VaadinIcon.CIRCLE_THIN.create());
    launchCircleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
    launchCircleButton.addClickListener(clk -> {
      launchCircleEditor();
    });
    imgToolBar.add(launchCircleButton);

    Button launchFreeformButton = new Button("Freeform");
    launchFreeformButton.setIcon(VaadinIcon.PENCIL.create());
    launchFreeformButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
    launchFreeformButton.addClickListener(clk -> {
      launchFreeformEditor();
    });
    imgToolBar.add(launchFreeformButton);

    TextField tf = new TextField();
    tf.setPlaceholder("Type text here and then click on the image");
    tf.setWidth("8em");
    tf.addValueChangeListener(val -> {
      if (!val.isFromClient())
        return;
      textValue = val.getValue().trim();
      launchTextEditor();
    });
    tf.addFocusListener(evt -> {
      if (!evt.isFromClient())
        return;
      launchTextEditor();
    });
    imgToolBar.add(tf);
    imgToolBar.add(createTextFontSizePicker());
    Button launchTextButton = new Button("Text");
    launchTextButton.setIcon(VaadinIcon.TEXT_LABEL.create());
    launchTextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
    launchTextButton.addClickListener(clk -> {
      if (textValue == null)
        textValue = "";
      textValue = textValue.trim();
      if (textValue.isEmpty()) {
        Notification.show("Set text first", 3000, Notification.Position.MIDDLE);
      } else {
        launchTextEditor();
      }
    });
    imgToolBar.add(launchTextButton);

    editorTitle = new Span();
    add(editorTitle);

    workSpace = new VerticalLayout();
    workSpace.setSizeFull();
    add(workSpace);

    // starts with image and freeform editor active ...
    init();
  }

  private void init() {
    workSpace.removeAll();
    qimgMkup = new QImgMkup();
    cfg = new QImgMkup.EditCfg();
    cfg.color = ColorRange.RED.colStr;
    cfg.thicknessPx = 8;
    cfg.editor = QImgMkup.Editor.FREEFORM;
    cfg.font_size = 30;
    qimgMkup.setEditCfg(cfg);
    Component c = qimgMkup.getImgMkupEditor();
    workSpace.add(c);
    startEditing();
  }

  private void setTitle(String title) {
    editorTitle.removeAll();
    title = "<b>" + title + "</b>";
    editorTitle.add(new Html(title));
  }

  private void launchFreeformEditor() {
    System.out.println("launchFreeformEditor ");
    setTitle("Freeform Editor ...");
    qimgMkup.addFreeform();
  }

  private void launchCircleEditor() {
    setTitle("Circle Editor TBD");
    // workSpace.removeAll();
    Notification.show("Circle Editor TBD", 2000, Notification.Position.MIDDLE);
  }

  private void launchLineEditor() {
    setTitle("Line Editor TBD");
    // workSpace.removeAll();
    Notification.show("Line Editor TBD", 2000, Notification.Position.MIDDLE);
  }

  private void launchTextEditor() {
    setTitle("Text Editor");
    // this needs to write to the image at the location of the next mouse click
    String text = textValue;
    qimgMkup.addText(text);
  }

  public void startEditing() {
    String imgAsURL = MockDataSource.PREPEND_FOR_URL + MockDataSource.JPG_AS_BASE64_STR;
    qimgMkup.setImage(imgAsURL);
  }

  public void finishEditing() {
    JpgReadyListenerImpl receiver = new JpgReadyListenerImpl();
    qimgMkup.registerForUpdatedImg(receiver);
  }

  private class JpgReadyListenerImpl implements QImgMkup.JpgReadyListener {

    @Override
    public void jpgAsBase64Str(String s) {

      // The edited image is returned here when it is available.

      if (s == null) {
        Notification.show("JPG String NULL", 3000, Notification.Position.MIDDLE);
        return;
      }
      if (s.isEmpty()) {
        Notification.show("JPG String EMPTY", 3000, Notification.Position.MIDDLE);
        return;
      }
      // does it get returned with PREPEND_FOR_URL ?
      if (s.startsWith(MockDataSource.PREPEND_FOR_URL)) {
        s = s.replace(MockDataSource.PREPEND_FOR_URL, "");
      }
      if (MockDataSource.JPG_AS_BASE64_STR.equals(s)) {
        Notification.show("JPG String UNCHANGED", 3000, Notification.Position.MIDDLE);
        return;
      }

      // finally ... the image seems ok so
      // let's launch it to have a look ...
      Image image = new Image();
      image.setSrc(s);
      VerticalLayout dui = new VerticalLayout();
      dui.add(new Span("==== THE MARKED UP IMAGE ===="));
      dui.add(image);
      Dialog d = new Dialog();
      d.add(dui);
      d.open();
    }
  }

  private enum ColorRange {
    BLACK("black"),
    WHITE("white"),
    RED("red"),
    YELLOW("yellow"),
    GREEN("green"),
    BLUE("blue"),
    PURPLE("purple");

    String colStr;

    ColorRange(String colStr) {
      this.colStr = colStr;
    }

    public String colorStr() {
      return colStr;
    }

    public static ColorRange fromStr(String s) {
      for (ColorRange c : ColorRange.values()) {
        if (c.colStr.equals(s))
          return c;
      }
      return ColorRange.RED; // default
    }
  }

  private Component createThicknessPicker() {
    IntegerField thickPick = new IntegerField();
    thickPick.setLabel("Line thickness");
    thickPick.setWidth("6em");
    thickPick.setMin(4);
    thickPick.setMax(16);
    thickPick.setStep(1);
    // thickPick.setHasControls(true);
    thickPick.setValue(cfg.thicknessPx);
    thickPick.addValueChangeListener(evt -> {
      int val = evt.getValue();
      cfg.thicknessPx = val;
      qimgMkup.setEditCfg(cfg);
    });
    return thickPick;
  }

  private Component createTextFontSizePicker() {
    IntegerField fontSizePick = new IntegerField();
    fontSizePick.setLabel("Letter Size");
    fontSizePick.setWidth("7em");
    fontSizePick.setMin(10);
    fontSizePick.setMax(70);
    fontSizePick.setStep(10);
    // fontSizePick.setHasControls(true);
    fontSizePick.setValue(cfg.font_size);
    fontSizePick.addValueChangeListener(evt -> {
      int val = evt.getValue();
      cfg.font_size = val;
      qimgMkup.setEditCfg(cfg);
    });
    return fontSizePick;
  }

  private Component createColorPicker() {
    ComboBox<ColorRange> colorList = new ComboBox();
    colorList.setWidth("7em");
    colorList.setRenderer(new ComponentRenderer<>(col -> {
      Icon colorPatch = new Icon(VaadinIcon.CIRCLE);
      colorPatch.setSize("20px");
      colorPatch.setColor(col.colStr);
      return colorPatch;
    }));
    colorList.setItems(ColorRange.values());
    ColorRange defaultCol = ColorRange.RED;
    currentColor = defaultCol;
    if (cfg != null) {
      defaultCol = ColorRange.fromStr(cfg.color); // init from cfg
    }
    colorList.setValue(defaultCol);
    colorList.addValueChangeListener(evt -> {
      ColorRange col = evt.getValue();
      cfg.color = col.colStr;
      qimgMkup.setEditCfg(cfg);
      currentColor = col;
    });
    return colorList;
  }

}
