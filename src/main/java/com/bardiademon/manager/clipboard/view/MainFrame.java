package com.bardiademon.manager.clipboard.view;

import com.bardiademon.manager.clipboard.ClipboardManagerApplication;
import com.bardiademon.manager.clipboard.data.entity.ClipboardEntity;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import com.bardiademon.manager.clipboard.manager.ClipboardManager;
import com.bardiademon.manager.clipboard.service.ClipboardService;
import com.bardiademon.manager.clipboard.util.ImageResizer;
import com.bardiademon.manager.clipboard.util.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainFrame {

    private final static Logger logger = LogManager.getLogger(MainFrame.class);

    private static Font persianFont, englishFont;

    public static boolean isRunning = false;
    public static boolean isDelete = false;

    private static DefaultListModel<ClipboardEntity> defaultListModel;
    private static JList<ClipboardEntity> list;

    private static ClipboardEntity clipboardSelected = null;
    private final static Map<Integer, ImageIcon> clipboardImages = new HashMap<>();

    static {
        readFont();
    }

    private MainFrame() {
    }

    public static void update(boolean create) {
        logger.trace("Starting main frame update, Create: {}", create);

        ClipboardService.repository().fetchClipboards().onComplete(fetchClipboardsHandler -> {

            if (fetchClipboardsHandler.failed()) {
                logger.error("Failed fetch clipboard, Create: {}", create, fetchClipboardsHandler.cause());
                return;
            }

            List<ClipboardEntity> clipboardEntities = fetchClipboardsHandler.result();

            logger.trace("Successfully fetch clipboards, Create: {} , ClipboardEntities: {}", clipboardEntities, clipboardEntities);

            clipboardImages.clear();
            defaultListModel = new DefaultListModel<>();
            if (clipboardEntities != null) {
                defaultListModel.addAll(clipboardEntities);
            }
            if (list != null) {
                list.setModel(defaultListModel);
                list.repaint();
                list.revalidate();
            }

            if (!isRunning && create) {
                create();
            }
        });

    }

    private static void create() {
        if (isRunning) {
            return;
        }

        isRunning = true;

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Clipboard Manager [bardiademon]");
            frame.setSize(400, 450);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            frame.setLayout(null);

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    isRunning = false;
                }
            });

            Panel headerPanel = new Panel();
            headerPanel.setSize(frame.getWidth(), 50);
            headerPanel.setLayout(null);

            JLabel lblClipboard = new JLabel("Clipboard");
            lblClipboard.setSize(100, 25);
            lblClipboard.setFont(englishFont);
            lblClipboard.setBounds(10, 20, lblClipboard.getWidth(), lblClipboard.getHeight());
            headerPanel.add(lblClipboard);

            JButton btnClear = new JButton("Clear All");
            btnClear.addActionListener(e -> {
                if (isDelete) {
                    return;
                }
                isDelete = true;
                ClipboardService.repository().deleteAllClipboard().onComplete(deleteAllHandler -> {
                    isDelete = false;
                    if (deleteAllHandler.failed()) {
                        logger.error("Failed to delete all clipboards");
                        return;
                    }
                    logger.trace("Successfully delete all clipboards");
                    update(false);
                });
            });
            btnClear.setSize(90, 25);
            btnClear.setFont(englishFont);
            btnClear.setBounds(290, 20, btnClear.getWidth(), btnClear.getHeight());
            headerPanel.add(btnClear);
            JButton btnRemove = new JButton("Remove");
            btnRemove.addActionListener(e -> {
                if (isDelete || clipboardSelected == null) {
                    return;
                }
                isDelete = true;
                ClipboardService.repository().deleteClipboardById(clipboardSelected.getId()).onComplete(deleteHandler -> {
                    isDelete = false;
                    if (deleteHandler.failed()) {
                        logger.error("Failed to delete clipboard", deleteHandler.cause());
                        return;
                    }
                    logger.trace("Successfully delete clipboard");
                    update(false);
                });
            });
            btnRemove.setSize(90, 20);
            btnRemove.setFont(englishFont);
            btnRemove.setBounds(195, 20, btnClear.getWidth(), btnClear.getHeight());
            headerPanel.add(btnRemove);

            Panel mainPanel = new Panel();
            mainPanel.setSize(frame.getWidth() - 25, frame.getHeight() - 100);
            mainPanel.setLayout(new BorderLayout());

            list = new JList<>();
            list.setModel(defaultListModel);
            list.setBorder(BorderFactory.createEmptyBorder());
            list.setFont(englishFont.deriveFont(18F));
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setCellRenderer(new ClipboardListItem());

            JScrollPane scrollPane = new JScrollPane(list);
            scrollPane.setPreferredSize(new Dimension(mainPanel.getWidth(), mainPanel.getHeight()));
            scrollPane.setBorder(BorderFactory.createEmptyBorder());

            mainPanel.add(scrollPane, BorderLayout.CENTER);

            headerPanel.setBounds(0, 0, headerPanel.getWidth(), headerPanel.getHeight());
            frame.add(headerPanel);

            mainPanel.setBounds(5, 50, mainPanel.getWidth(), mainPanel.getHeight());
            frame.add(mainPanel);

            frame.setVisible(true);
            System.gc();
        });
    }

    private static void readFont() {
        try {
            InputStream persianFontInputStream = ClipboardManagerApplication.getResource(Paths.RESOURCE_PERSIAN_FONT);
            if (persianFontInputStream == null) {
                throw new FileNotFoundException(Paths.RESOURCE_PERSIAN_FONT);
            }

            InputStream englishFontInputStream = ClipboardManagerApplication.getResource(Paths.RESOURCE_ENGLISH_FONT);
            if (englishFontInputStream == null) {
                throw new FileNotFoundException(Paths.RESOURCE_ENGLISH_FONT);
            }

            persianFont = Font.createFont(Font.TRUETYPE_FONT, persianFontInputStream).deriveFont(12F);
            englishFont = Font.createFont(Font.TRUETYPE_FONT, englishFontInputStream).deriveFont(12F);

            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(persianFont);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(englishFont);

        } catch (FontFormatException | IOException e) {
            logger.error("Failed to create font", e);
        }
    }

    private final static class ClipboardListItem implements ListCellRenderer<ClipboardEntity> {

        public ClipboardListItem() {
        }

        private JPanel createItem(ClipboardEntity clipboardEntity, Dimension rootSize, int index, boolean isSelected) {
            JPanel panel = new JPanel();

            panel.setLayout(new BorderLayout());
            panel.setPreferredSize(new Dimension((int) rootSize.getWidth(), 80));
            Border border = BorderFactory.createLineBorder(panel.getBackground(), 1);
            Border margin = new EmptyBorder(2, 2, 2, 2);
            panel.setBorder(BorderFactory.createCompoundBorder(border, margin));

            JLabel lblClipboardData = new JLabel();
            lblClipboardData.setOpaque(true);
            lblClipboardData.setBorder(BorderFactory.createEmptyBorder());

            if (clipboardEntity.getType().equals(ClipboardType.IMAGE)) {

                if (clipboardImages.containsKey(index)) {
                    ImageIcon imageIcon = clipboardImages.get(index);
                    if (imageIcon == null) {
                        lblClipboardData.setText("❌ Failed to load image");
                        clipboardImages.remove(index);
                    } else {
                        lblClipboardData.setText("");
                        lblClipboardData.setIcon(imageIcon);
                    }

                } else {
                    lblClipboardData.setText("⏳ Loading Image...");
                    new SwingWorker<ImageIcon, Void>() {
                        @Override
                        protected ImageIcon doInBackground() {
                            try {
                                BufferedImage image = ImageResizer.resizeWithoutCrop(ImageIO.read(new FileInputStream(clipboardEntity.getData())), (int) rootSize.getWidth(), (int) panel.getPreferredSize().getHeight());
                                return new ImageIcon(image);
                            } catch (Exception e) {
                                logger.error("Failed to load image file, ClipboardEntity: {}", clipboardEntity, e);
                                return null;
                            }
                        }

                        @Override
                        protected void done() {
                            try {
                                ImageIcon imageIcon = get();
                                clipboardImages.put(index, imageIcon);
                            } catch (InterruptedException | ExecutionException e) {
                                lblClipboardData.setText("Failed to load image file");
                                clipboardImages.put(index, null);
                            } finally {
                                list.repaint();
                                System.gc();
                            }
                        }
                    }.execute();
                }

            } else {
                clipboardImages.remove(index);

                String text = """
                        <html>
                            <body>
                                <div style='font-family: ::FONTS::;font-size:10px;white-space: pre;::UNDERLINE::;'>::TEXT::</div>
                            </body>
                        </html>
                        """
                        .replace("::FONTS::", String.format("\"%s\", \"%s\"", persianFont.getFamily(), englishFont.getFamily()))
                        .replace("::TEXT::", clipboardEntity.toString()
                                .replace("&", "&amp;")
                                .replace("<", "&lt;")
                                .replace(">", "&gt;"))
                        .replace("::UNDERLINE::;", isSelected ? "text-decoration: underline;" : "");

                logger.trace("Text: {}", text);
                lblClipboardData.setText(text);
            }

            panel.add(lblClipboardData, BorderLayout.CENTER);

            return panel;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ClipboardEntity> list, ClipboardEntity value, int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                clipboardSelected = value;
                setClipboard(value);
            }
            return createItem(value, list.getSize(), index, isSelected);
        }
    }

    private static void setClipboard(ClipboardEntity clipboardEntity) {
        ClipboardManager.setClipboard(clipboardEntity);
    }

}
