package com.bardiademon.manager.clipboard.view;

import com.bardiademon.manager.clipboard.ClipboardManagerApplication;
import com.bardiademon.manager.clipboard.data.entity.ClipboardEntity;
import com.bardiademon.manager.clipboard.manager.ClipboardManager;
import com.bardiademon.manager.clipboard.service.ClipboardService;
import com.bardiademon.manager.clipboard.util.Paths;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class MainFrame {

    private static Font persianFont, englishFont;

    public static boolean isRunning = false;
    public static boolean isClearAll = false;

    private static DefaultListModel<ClipboardEntity> defaultListModel;
    private static JList<ClipboardEntity> list;

    private static ClipboardEntity clipboardSelected = null;

    static {
        readFont();
    }

    private MainFrame() {
    }

    public static void update(boolean create) {
        if (!isRunning && !create) {
            return;
        }
        List<ClipboardEntity> clipboardEntities = ClipboardService.repository().fetchClipboards();
        defaultListModel = new DefaultListModel<>();
        if (clipboardEntities != null) {
            defaultListModel.addAll(clipboardEntities);
        }
        if (list != null) {
            list.setModel(defaultListModel);
            list.repaint();
            list.revalidate();
        }
        if (create) {
            create();
        }
    }

    private static void create() {
        if (isRunning) {
            return;
        }

        isRunning = true;

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Clipboard Manager [bardiademon]");
            frame.setSize(350, 450);
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
            lblClipboard.setSize(100, 20);
            lblClipboard.setFont(englishFont);
            lblClipboard.setBounds(10, 20, lblClipboard.getWidth(), lblClipboard.getHeight());
            headerPanel.add(lblClipboard);

            JButton btnClear = new JButton("Clear All");
            btnClear.addActionListener(e -> {

                if (isClearAll) {
                    return;
                }

                isClearAll = true;
                new Thread(() -> {
                    ClipboardService.repository().deleteAllClipboard();
                    update(false);
                    isClearAll = false;
                }).start();

            });
            btnClear.setSize(90, 20);
            btnClear.setFont(englishFont);
            btnClear.setBounds(240, 20, btnClear.getWidth(), btnClear.getHeight());
            headerPanel.add(btnClear);
            JButton btnRemove = new JButton("Remove");
            btnRemove.addActionListener(e -> {
                if (clipboardSelected != null) {
                    ClipboardService.repository().deleteClipboardById(clipboardSelected.getId());
                    clipboardSelected = null;
                    update(false);
                    list.setSelectedIndex(0);
                }
            });
            btnRemove.setSize(90, 20);
            btnRemove.setFont(englishFont);
            btnRemove.setBounds(150, 20, btnClear.getWidth(), btnClear.getHeight());
            headerPanel.add(btnRemove);

            Panel mainPanel = new Panel();
            mainPanel.setSize(frame.getWidth() - 25, frame.getHeight() - 100);
            mainPanel.setLayout(new BorderLayout());

            list = new JList<>();
            list.setModel(defaultListModel);
            list.setFont(englishFont.deriveFont(18F));
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setCellRenderer(new ClipboardListItem());

            JScrollPane scrollPane = new JScrollPane(list);
            scrollPane.setPreferredSize(new Dimension(mainPanel.getWidth(), mainPanel.getHeight()));

            mainPanel.add(scrollPane, BorderLayout.CENTER);

            headerPanel.setBounds(0, 0, headerPanel.getWidth(), headerPanel.getHeight());
            frame.add(headerPanel);

            mainPanel.setBounds(5, 50, mainPanel.getWidth(), mainPanel.getHeight());
            frame.add(mainPanel);

            frame.setVisible(true);
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
            System.out.println("Failed to create font, Exception: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    private final static class ClipboardListItem implements ListCellRenderer<ClipboardEntity> {

        public ClipboardListItem() {
        }

        private JPanel createItem(ClipboardEntity clipboardEntity, Dimension rootSize) {
            JPanel panel = new JPanel();

            panel.setLayout(new BorderLayout());
            panel.setPreferredSize(new Dimension((int) rootSize.getWidth(), 80));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            String text = """
                    <html>
                        <body>
                            <span style='font-family: ::FONTS::'>::TEXT::</span>
                        </body>
                    </html>
                    """
                    .replace("::FONTS::", persianFont.getFamily())
                    .replace("::TEXT::", clipboardEntity.toString());

            JLabel lblClipboardData = new JLabel(text);
            lblClipboardData.setFont(persianFont.deriveFont(15F));

            panel.add(lblClipboardData, BorderLayout.CENTER);

            return panel;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ClipboardEntity> list, ClipboardEntity value, int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                clipboardSelected = value;
                setClipboard(value);
            }
            return createItem(value, list.getSize());
        }
    }

    private static void setClipboard(ClipboardEntity clipboardEntity) {
        Objects.requireNonNull(ClipboardManager.manager()).setClipboard(clipboardEntity);
    }

}
