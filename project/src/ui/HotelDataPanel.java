package ui;

import javax.swing.*;
import java.awt.*;

public abstract class HotelDataPanel extends JPanel {
    protected JLabel titleLabel;
    protected JButton refreshButton;
    protected JButton searchButton;
    protected JTextField searchField;   //  quick filter
    protected JTextArea detailArea;
    protected JPanel rightPanel;

    public HotelDataPanel(String title) {
        setLayout(new BorderLayout());
        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        // Top panel: title left, search + refresh right
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchField = new JTextField(15);
        searchField.setToolTipText("Type to filter...");
        searchButton = new JButton("Search");
        refreshButton = new JButton("Refresh");

        rightPanel.add(new JLabel("Search:"));
        rightPanel.add(searchField);
        rightPanel.add(refreshButton);
        rightPanel.add(searchButton);

        topPanel.add(rightPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    }

    /**
     * Allows subclasses to set a popup menu for the search button.
     * @param menu the popup menu to show when the search button is clicked
     */
    protected void setSearchMenu(JPopupMenu menu) {
        searchButton.addActionListener(e -> {
            menu.show(searchButton, 0, searchButton.getHeight());
        });
    }

    protected abstract void initComponents();

    protected JSplitPane createMainWithDetails(JComponent mainComponent) {
        JScrollPane mainScroll = new JScrollPane(mainComponent);
        JScrollPane detailsScroll = new JScrollPane(detailArea);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                mainScroll, detailsScroll);
        split.setResizeWeight(0.7);
        return split;
    }

    protected void showDetails(String text) {
        detailArea.setText(text);
    }
}
