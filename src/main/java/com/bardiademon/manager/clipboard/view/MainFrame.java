package com.bardiademon.manager.clipboard.view;

import com.bardiademon.manager.clipboard.data.entity.ClipboardEntity;
import com.bardiademon.manager.clipboard.service.ClipboardService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class MainFrame {

    private static Font defaultFont;

    public static boolean isRunning = false;
    public static boolean isClearAll = false;

    private static DefaultListModel<ClipboardEntity> defaultListModel;
    private static JList<ClipboardEntity> list;

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
            if (list != null) {
                list.setModel(defaultListModel);
            }
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
            lblClipboard.setFont(defaultFont);
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
            btnClear.setFont(defaultFont);
            btnClear.setBounds(240, 20, btnClear.getWidth(), btnClear.getHeight());
            headerPanel.add(btnClear);

            Panel mainPanel = new Panel();
            mainPanel.setSize(frame.getWidth() - 25, frame.getHeight() - 100);
            mainPanel.setLayout(new BorderLayout());

            list = new JList<>();
            list.setModel(defaultListModel);
            list.setFont(defaultFont.deriveFont(15F));
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
        URL systemResource = ClassLoader.getSystemResource("font/PoppinsMedium.ttf");
        if (systemResource == null) {
            System.out.println("Not found font");
            return;
        }

        InputStream fontInputStream;
        try {
            fontInputStream = systemResource.openStream();
        } catch (IOException e) {
            System.out.println("Failed to open font stream, Exception: " + e.getMessage());
            e.printStackTrace(System.out);
            return;
        }

        try {
            defaultFont = Font.createFont(Font.TRUETYPE_FONT, fontInputStream).deriveFont(12F);
        } catch (FontFormatException | IOException e) {
            System.out.println("Failed to create font, Exception: " + e.getMessage());
            e.printStackTrace(System.out);
            return;
        }

    }

}
