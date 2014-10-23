import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class Window extends ApplicationWindow {
  
  private Canvas encodeCanvas;
  private Canvas decodeCanvas;
  private TextViewer encodeTextViewer;
  private TextViewer decodeTextViewer;
  private Spinner bitsSpinner;
  private Text maxCharsText;
  private Text currentCharsText;
  
  private ImageData sourceImage;
  private ImageData encodedImage;
  private Document encodeDoc = new Document();
  private Document decodeDoc = new Document();

  private int maxChars;

  /**
   * Create the application window.
   */
  public Window() {
    super(null);
    createActions();
    //addToolBar(SWT.FLAT | SWT.WRAP);
    //addMenuBar();
    //addStatusLine();
  }

  /**
   * Create contents of the application window.
   * @param parent
   */
  @Override
  protected Control createContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(new GridLayout(2, false));
    
    TabFolder tabFolder = new TabFolder(container, SWT.NONE);
    tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));
    
    //ENCODE
    
    TabItem tbtmEncode = new TabItem(tabFolder, SWT.NONE);
    tbtmEncode.setText("Encode");
    
    Composite composite_1 = new Composite(tabFolder, SWT.NONE);
    tbtmEncode.setControl(composite_1);
    composite_1.setLayout(new GridLayout(5, false));
    
    encodeCanvas = new Canvas(composite_1, SWT.BORDER);
    encodeCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    encodeCanvas.addPaintListener(new PaintListener() {

      @Override
      public void paintControl(PaintEvent e) {
        drawImage(encodeCanvas.getBounds(), e.gc, encodedImage);
      }
    });
    
    encodeTextViewer = new TextViewer(composite_1, SWT.BORDER | SWT.V_SCROLL);
    StyledText styledText = encodeTextViewer.getTextWidget();
    styledText.setWordWrap(true);
    styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 4, 1));
    encodeTextViewer.setDocument(encodeDoc);
    
    encodeDoc.addDocumentListener(new IDocumentListener() {

      @Override
      public void documentAboutToBeChanged(DocumentEvent event) {
        // Do nothing
      }

      @Override
      public void documentChanged(DocumentEvent event) {
        currentCharsText.setText(Integer.toString(encodeDoc.getLength()));
        updateEncoding();
      }
      
    });
    
    Button btnOpenImage = new Button(composite_1, SWT.NONE);
    btnOpenImage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    btnOpenImage.setText("Open Image");
    btnOpenImage.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        openImage();
      }
    });
    
    Button btnOpenTextFile = new Button(composite_1, SWT.NONE);
    btnOpenTextFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
    btnOpenTextFile.setText("Open Text");
    btnOpenTextFile.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        openText();
      }
    });
    
    Button btnSaveImage = new Button(composite_1, SWT.NONE);
    btnSaveImage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    btnSaveImage.setText("Save Image");    
    
    Label lblNewLabel = new Label(composite_1, SWT.NONE);
    lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblNewLabel.setText("Characters");
    
    currentCharsText = new Text(composite_1, SWT.BORDER);
    currentCharsText.setText("0");
    GridData gd_text = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
    gd_text.widthHint = 40;
    currentCharsText.setEditable(false);
    currentCharsText.setLayoutData(gd_text);
    
    Label lblMaximumCharacters = new Label(composite_1, SWT.NONE);
    lblMaximumCharacters.setText("Maximum");
    
    maxCharsText = new Text(composite_1, SWT.BORDER);
    GridData gd_maxCharsText = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
    gd_maxCharsText.widthHint = 40;
    maxCharsText.setLayoutData(gd_maxCharsText);
    maxCharsText.setEditable(false);
    maxCharsText.setText("0");
    btnSaveImage.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        saveImage();
      }
    });
    
    //DECODE
    
    TabItem tbtmDecode = new TabItem(tabFolder, SWT.NONE);
    tbtmDecode.setText("Decode");
    
    Composite decodeComposite = new Composite(tabFolder, SWT.NONE);
    tbtmDecode.setControl(decodeComposite);
    decodeComposite.setLayout(new GridLayout(2, false));
    
    decodeCanvas = new Canvas(decodeComposite, SWT.BORDER);
    decodeCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    decodeCanvas.addPaintListener(new PaintListener() {

      @Override
      public void paintControl(PaintEvent e) {
        drawImage(decodeCanvas.getBounds(), e.gc, sourceImage);
      }
    });
    
    decodeTextViewer = new TextViewer(decodeComposite, SWT.BORDER | SWT.V_SCROLL);
    StyledText decodeStyledText = decodeTextViewer.getTextWidget();
    decodeStyledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    decodeStyledText.setWordWrap(true);
    decodeTextViewer.setDocument(decodeDoc);
    decodeTextViewer.setEditable(false);
    decodeDoc.addDocumentListener(new IDocumentListener() {

      @Override
      public void documentAboutToBeChanged(DocumentEvent event) {
        // Do nothing
      }

      @Override
      public void documentChanged(DocumentEvent event) {
        decodeTextViewer.refresh();
      }
      
    });
    
    Button decodeBtnOpenImage = new Button(decodeComposite, SWT.NONE);
    decodeBtnOpenImage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    decodeBtnOpenImage.setText("Open Image");
    decodeBtnOpenImage.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        openImage();
      }
    });
    
    Button decodeBtnSaveText = new Button(decodeComposite, SWT.NONE);
    decodeBtnSaveText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    decodeBtnSaveText.setText("Save Text");
    decodeBtnSaveText.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        saveText();
      }
    });
    
    // GENERAL
    
    Composite composite = new Composite(container, SWT.NONE);
    composite.setLayout(new GridLayout(2, false));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    
    Label lblBitsUsed = new Label(composite, SWT.NONE);
    lblBitsUsed.setText("Bits Used");
    
    bitsSpinner = new Spinner(composite, SWT.BORDER);
    GridData gd_spinner = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
    gd_spinner.widthHint = 20;
    bitsSpinner.setLayoutData(gd_spinner);
    bitsSpinner.setMaximum(8);
    bitsSpinner.setSelection(4);
    bitsSpinner.setPageIncrement(2);
    bitsSpinner.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updateMaxChars();
      }
    });

    return container;
  }

  /**
   * Create the actions.
   */
  private void createActions() {
    // Create the actions
  }

  /**
   * Create the menu manager.
   * @return the menu manager
   */
  @Override
  protected MenuManager createMenuManager() {
    MenuManager menuManager = new MenuManager("menu");
    return menuManager;
  }

  /**
   * Create the toolbar manager.
   * @return the toolbar manager
   */
  @Override
  protected ToolBarManager createToolBarManager(int style) {
    ToolBarManager toolBarManager = new ToolBarManager(style);
    return toolBarManager;
  }

  /**
   * Create the status line manager.
   * @return the status line manager
   */
  @Override
  protected StatusLineManager createStatusLineManager() {
    StatusLineManager statusLineManager = new StatusLineManager();
    return statusLineManager;
  }

  /**
   * Launch the application.
   * @param args
   */
  public static void main(String args[]) {
    try {
      Window window = new Window();
      window.setBlockOnOpen(true);
      window.open();
      Display.getCurrent().dispose();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Configure the shell.
   * @param newShell
   */
  @Override
  protected void configureShell(Shell newShell) {
    newShell.setMinimumSize(getInitialSize());
    super.configureShell(newShell);
    newShell.setText("Assignment 2, Garrett Smith 3018390");
    // hide the weird extra seperator
    getSeperator1().setVisible(false);
  }

  /**
   * Return the initial size of the window.
   */
  @Override
  protected Point getInitialSize() {
    return new Point(600, 450);
  }
  
  protected void openImage() {
    // create the dialog to select an image file
    FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
    dialog.setText("Select an image");
    dialog.setFilterExtensions(new String[]{"*.jpg;*.png;*.gif;*.bmp"});
    
    // get a file path
    String path = dialog.open();

    // open the file with the application
    if (path != null) {
      // set image
      sourceImage = new ImageData(path);
      updateMaxChars();
      update();
    }
  }
  
  protected void openText() {
    // create the dialog to select a text file
    FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
    dialog.setText("Select a text file");
    dialog.setFilterExtensions(new String[]{"*.txt"});
    
    // get a file path
    String path = dialog.open();

    // open the file with the application
    if (path != null) {
      try {
        // read in the text file
        Scanner scan = new Scanner(new File(path));
        StringBuilder bld = new StringBuilder();
        while (scan.hasNextLine()) {
          bld.append(scan.nextLine());
          bld.append('\n');
        }
        encodeDoc.set(bld.toString());
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      updateEncoding();
    }
  }
  
  protected void saveImage() {
    FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
    dialog.setText("Select where to save the image");
    dialog.setFilterExtensions(new String[]{"*.bmp"});
    
    // get a file path
    String path = dialog.open();
    
    File file = new File(path);
    
    boolean existed = false;
    
    // create the empty file if it does not exist
    try {
      existed = !file.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
      MessageDialog.openError(
          getShell(), 
          "Failed to save image", 
          "File could not be written.");
      return;
    }
    
    // if we can write to the file
    if (file.canWrite()) {
      
      // confirm overwriting
      if (existed) {
        // if the user chooses not to overwrite return
        if (!MessageDialog.openQuestion(
            getShell(), 
            "Confirmation",
            "Are you sure would like to overwrite this file?")) {
        }
      }
      
      // save the image
      System.out.println("Saving " + path);
      ImageLoader imgLoader = new ImageLoader();
      imgLoader.compression = 100;
      imgLoader.data = new ImageData[] {encodedImage};
      imgLoader.save(path, SWT.IMAGE_BMP);
    }
    else {
      MessageDialog.openError(
          getShell(), 
          "Failed to save image", 
          "File could not be written.");
    }
  }
  
  protected void saveText() {
    FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
    dialog.setText("Select where to save the text");
    
    // get a file path
    String path = dialog.open();
    
    File file = new File(path);
    
    boolean existed = false;
    
    // create the empty file if it does not exist
    try {
      existed = !file.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
      MessageDialog.openError(
          getShell(), 
          "Failed to save text", 
          "File could not be written.");
      return;
    }
    
    // if we can write to the file
    if (file.canWrite()) {
      
      // confirm overwriting
      if (existed) {
        // if the user chooses not to overwrite return
        if (!MessageDialog.openQuestion(
            getShell(), 
            "Confirmation",
            "Are you sure would like to overwrite this file?")) {
        }
      }
      
      // save the text
      System.out.println("Saving " + path);
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(decodeDoc.get());
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
        MessageDialog.openError(
            getShell(), 
            "Failed to save text", 
            "File could not be written.");
        return;
      }
    }
    else {
      MessageDialog.openError(
          getShell(), 
          "Failed to save image", 
          "File could not be written.");
    }
  }
  
  protected void updateMaxChars() {
    if (sourceImage != null) {
      maxChars = ImageSteg.maximumChars(bitsSpinner.getSelection(), sourceImage.data);
      maxCharsText.setText(Integer.toString(maxChars));
      update();
    }
  }
  
  protected void update() {
    updateEncoding();
    updateDecoding();
  }
  
  protected void updateEncoding() {
    if (sourceImage != null) {
      int n = bitsSpinner.getSelection();
      String msg = encodeDoc.get();
      byte[] bytes = sourceImage.data;
      bytes = ImageSteg.encode(n, msg, bytes);

      ImageData data = (ImageData) sourceImage.clone();
      data.data = bytes;
      encodedImage = data;

      encodeCanvas.redraw();
    }
  }
  
  protected void updateDecoding() {
    if (sourceImage != null) {
      int n = bitsSpinner.getSelection();
      byte[] bytes = sourceImage.data;
      String msg = ImageSteg.decode(n, bytes);
      // check if the message is just garbage
//      int strangeChars = 0;
//      int checkLength = Math.min(msg.length(), 20);
//      for (int i = 0; i < 20; i++) {
//        char c = msg.charAt(i);
//        if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) {
//          strangeChars++;
//        }
//      }
//      if (strangeChars < checkLength / 2) {
//        decodeDoc.set(msg);        
//      }
//      else {
//        decodeDoc.set("There does not appear to be a valid message");
//      }
      decodeDoc.set(msg);
      decodeCanvas.redraw();
    }
  }
  
  private void drawImage(Rectangle canvasBounds, GC gc, ImageData data) {
    if (data != null) {
      Image img = new Image(Display.getCurrent(), data);
      
      Rectangle imageBounds = img.getBounds();

      float width = canvasBounds.width;
      float height = canvasBounds.height;
      
      float scaleX = width / imageBounds.width;
      float scaleY = height / imageBounds.height;
      float scale = Math.min(scaleX, scaleY);
      scale = Math.min(scale, 1); 
      
      int drawWidth = Math.round(imageBounds.width * scale);
      int drawHeight = Math.round(imageBounds.height * scale);
      int drawX = Math.round((canvasBounds.width - drawWidth) / 2);
      int drawY = Math.round((canvasBounds.height - drawHeight) / 2);
      
    //draw background
      // fill with black
      gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
      gc.fillRectangle(0, 0, canvasBounds.width, canvasBounds.height);
      // draw gradient on bottom
      gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
      gc.setBackground(new Color(Display.getCurrent(), 60, 60, 60));
      gc.fillGradientRectangle(
          0, 
          canvasBounds.height/2, 
          canvasBounds.width, 
          canvasBounds.height/2, 
          true);
      
      gc.drawImage(
          img, 
          0, 
          0, 
          imageBounds.width, 
          imageBounds.height, 
          drawX, 
          drawY, 
          drawWidth, 
          drawHeight);
    }
  }
}
