import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.DecimalFormat;

public class GUI {

    static JLabel statusLabel;
    static JProgressBar progressBar;
    static JTextField inputField;
    static JButton removeButton;
    static JSlider strengthSlider;
    static JLabel strengthValueLabel;
    static JPanel resultsPanel;
    static JLabel resultFileLabel;
    static JLabel resultSizeLabel;
    static JLabel resultDurationLabel;
    static JLabel resultStrengthLabel;
    static JButton openFolderButton;

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        JFrame frame = new JFrame("Background Noise Remover");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(540, 580);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout(0, 0));

        // ── TITLE BAR ────────────────────────────────────────────
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(30, 78, 121));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Background Noise Remover");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Powered by Spectral Subtraction");
        subtitle.setFont(new Font("Arial", Font.ITALIC, 12));
        subtitle.setForeground(new Color(180, 210, 240));
        JPanel titleText = new JPanel(new GridLayout(2, 1, 0, 2));
        titleText.setOpaque(false);
        titleText.add(title);
        titleText.add(subtitle);
        titlePanel.add(titleText, BorderLayout.CENTER);
        frame.add(titlePanel, BorderLayout.NORTH);

        // ── MAIN CONTENT ─────────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(245, 248, 252));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ── FILE SELECTION CARD ──────────────────────────────────
        JPanel fileCard = createCard("1.  Select Input File");
        JPanel fileRow = new JPanel(new BorderLayout(8, 0));
        fileRow.setOpaque(false);
        inputField = new JTextField("No file selected");
        inputField.setEditable(false);
        inputField.setFont(new Font("Arial", Font.PLAIN, 13));
        inputField.setForeground(new Color(100, 100, 100));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 225), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        inputField.setBackground(Color.WHITE);
        JButton browseBtn = new JButton("Browse...");
        browseBtn.setFont(new Font("Arial", Font.BOLD, 13));
        browseBtn.setBackground(new Color(46, 117, 182));
        browseBtn.setForeground(Color.WHITE);
        browseBtn.setFocusPainted(false);
        browseBtn.setBorderPainted(false);
        browseBtn.setPreferredSize(new Dimension(100, 34));
        browseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("MP4 / WAV files", "mp4", "wav"));
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                inputField.setText(chooser.getSelectedFile().getAbsolutePath());
                inputField.setForeground(new Color(30, 30, 30));
            }
        });
        fileRow.add(inputField, BorderLayout.CENTER);
        fileRow.add(browseBtn, BorderLayout.EAST);
        fileCard.add(fileRow);
        content.add(fileCard);
        content.add(Box.createVerticalStrut(12));

        // ── STRENGTH SLIDER CARD ─────────────────────────────────
        JPanel sliderCard = createCard("2.  Noise Removal Strength");

        JLabel sliderDesc = new JLabel("Higher strength removes more noise but may affect voice quality.");
        sliderDesc.setFont(new Font("Arial", Font.PLAIN, 12));
        sliderDesc.setForeground(new Color(100, 100, 100));
        sliderCard.add(sliderDesc);
        sliderCard.add(Box.createVerticalStrut(12));

        // Slider: 0–6 ticks → 0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0
        strengthSlider = new JSlider(JSlider.HORIZONTAL, 0, 6, 3);
        strengthSlider.setMajorTickSpacing(1);
        strengthSlider.setSnapToTicks(true);
        strengthSlider.setPaintTicks(true);
        strengthSlider.setOpaque(false);

        java.util.Hashtable<Integer, JLabel> labels = new java.util.Hashtable<>();
        String[] vals = {"0", "0.5", "1", "1.5", "2", "2.5", "3"};
        for (int i = 0; i <= 6; i++) {
            JLabel lbl = new JLabel(vals[i]);
            lbl.setFont(new Font("Arial", Font.PLAIN, 10));
            lbl.setForeground(new Color(80, 80, 80));
            labels.put(i, lbl);
        }
        strengthSlider.setLabelTable(labels);
        strengthSlider.setPaintLabels(true);

        // Current value badge
        strengthValueLabel = new JLabel("1.5");
        strengthValueLabel.setFont(new Font("Arial", Font.BOLD, 18));
        strengthValueLabel.setForeground(new Color(241, 196, 15));
        strengthValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        strengthValueLabel.setPreferredSize(new Dimension(50, 40));

        strengthSlider.addChangeListener(e -> {
            double val = strengthSlider.getValue() * 0.5;
            DecimalFormat df = new DecimalFormat("0.#");
            strengthValueLabel.setText(df.format(val));
            updateStrengthColor(val);
        });

        // Slider with min/max labels
        JPanel sliderWithLabels = new JPanel(new BorderLayout(4, 0));
        sliderWithLabels.setOpaque(false);
        sliderWithLabels.add(strengthSlider, BorderLayout.CENTER);

        // Slider + badge side by side
        JPanel sliderAndBadge = new JPanel(new BorderLayout(12, 0));
        sliderAndBadge.setOpaque(false);
        sliderAndBadge.add(sliderWithLabels, BorderLayout.CENTER);
        sliderAndBadge.add(strengthValueLabel, BorderLayout.EAST);
        sliderCard.add(sliderAndBadge);
        sliderCard.add(Box.createVerticalStrut(10));

        // Color hint chips
        JPanel hintRow = new JPanel(new GridLayout(1, 3, 4, 0));
        hintRow.setOpaque(false);
        String[] hints = {"Light  (0 – 1)", "Medium  (1 – 2)", "Strong  (2 – 3)"};
        Color[] hintColors = {
            new Color(39, 174, 96),
            new Color(241, 196, 15),
            new Color(231, 76, 60)
        };
        for (int i = 0; i < 3; i++) {
            JPanel chip = new JPanel(new BorderLayout());
            chip.setBackground(hintColors[i]);
            chip.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            JLabel chipLbl = new JLabel(hints[i], SwingConstants.CENTER);
            chipLbl.setFont(new Font("Arial", Font.PLAIN, 10));
            chipLbl.setForeground(Color.WHITE);
            chip.add(chipLbl, BorderLayout.CENTER);
            hintRow.add(chip);
        }
        sliderCard.add(hintRow);
        content.add(sliderCard);
        content.add(Box.createVerticalStrut(12));

        // ── PROCESS CARD ─────────────────────────────────────────
        JPanel processCard = createCard("3.  Remove Noise");

        removeButton = new JButton("Convert + Remove Noise");
        removeButton.setFont(new Font("Arial", Font.BOLD, 15));
        removeButton.setBackground(new Color(30, 78, 121));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.setBorderPainted(false);
        removeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeButton.addActionListener(e -> processAudio(frame));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Arial", Font.PLAIN, 11));
        progressBar.setForeground(new Color(46, 117, 182));
        progressBar.setBackground(new Color(220, 230, 242));
        progressBar.setBorderPainted(false);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

        statusLabel = new JLabel("Select a file and click the button to begin.");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 100, 100));

        processCard.add(removeButton);
        processCard.add(Box.createVerticalStrut(10));
        processCard.add(progressBar);
        processCard.add(Box.createVerticalStrut(6));
        processCard.add(statusLabel);
        content.add(processCard);
        content.add(Box.createVerticalStrut(12));

        // ── RESULTS CARD ─────────────────────────────────────────
        resultsPanel = createCard("4.  Cleaned Audio");
        resultsPanel.setVisible(false);

        JPanel resultGrid = new JPanel(new GridLayout(4, 2, 8, 6));
        resultGrid.setOpaque(false);
        resultGrid.add(makeResultKey("Output File:"));
        resultFileLabel = makeResultVal("—");
        resultGrid.add(resultFileLabel);
        resultGrid.add(makeResultKey("File Size:"));
        resultSizeLabel = makeResultVal("—");
        resultGrid.add(resultSizeLabel);
        resultGrid.add(makeResultKey("Duration:"));
        resultDurationLabel = makeResultVal("—");
        resultGrid.add(resultDurationLabel);
        resultGrid.add(makeResultKey("Strength Used:"));
        resultStrengthLabel = makeResultVal("—");
        resultGrid.add(resultStrengthLabel);

        openFolderButton = new JButton("Open Output Folder");
        openFolderButton.setFont(new Font("Arial", Font.BOLD, 13));
        openFolderButton.setBackground(new Color(39, 174, 96));
        openFolderButton.setForeground(Color.WHITE);
        openFolderButton.setFocusPainted(false);
        openFolderButton.setBorderPainted(false);
        openFolderButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        openFolderButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        resultsPanel.add(resultGrid);
        resultsPanel.add(Box.createVerticalStrut(10));
        resultsPanel.add(openFolderButton);
        content.add(resultsPanel);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // ── PROCESS AUDIO ─────────────────────────────────────────────
    static void processAudio(JFrame frame) {
        String inputPath = inputField.getText();
        if (inputPath.equals("No file selected")) {
            JOptionPane.showMessageDialog(frame, "Please select an MP4 or WAV file first.",
                "No File", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double strength = strengthSlider.getValue() * 0.5;
        resultsPanel.setVisible(false);
        removeButton.setEnabled(false);
        progressBar.setValue(0);

        SwingWorker<String, String> worker = new SwingWorker<>() {
            protected String doInBackground() throws Exception {
                publish("Converting to WAV...");
                progressBar.setValue(10);
                String wavPath = inputPath.endsWith(".wav")
                    ? inputPath : WAV.convert(inputPath);

                publish("Reading audio...");
                progressBar.setValue(25);
                float[] audio = AudioReader.readWav(wavPath);

                publish("Slicing frames...");
                progressBar.setValue(35);
                float[][] frames = FrameSlicer.slice(audio, 1024, 512);

                publish("Estimating noise...");
                progressBar.setValue(50);
                double[] noiseProfile = NoiseRemover.estimateNoise(frames, 10);

                publish("Removing noise (strength: " + strength + ")...");
                progressBar.setValue(65);
                double[][] cleanedFrames = new double[frames.length][];
                for (int i = 0; i < frames.length; i++)
                    cleanedFrames[i] = NoiseRemover.cleanFrame(frames[i], noiseProfile, strength);

                publish("Saving clean audio...");
                progressBar.setValue(85);
                float[] cleanAudio = AudioWriter.overlapAdd(cleanedFrames, 512);

                String cleanOutput = inputPath.contains(".")
                    ? inputPath.substring(0, inputPath.lastIndexOf('.')) + "_clean.wav"
                    : inputPath + "_clean.wav";

                AudioWriter.saveWav(cleanAudio, cleanOutput, 48000, 2);
                progressBar.setValue(100);
                publish("Done!");
                return cleanOutput;
            }

            protected void process(java.util.List<String> chunks) {
                statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            protected void done() {
                removeButton.setEnabled(true);
                try {
                    String outputPath = get();
                    showResults(outputPath, strength, frame);
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(frame,
                        "Something went wrong:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // ── SHOW RESULTS ──────────────────────────────────────────────
    static void showResults(String outputPath, double strength, JFrame frame) {
        File outFile = new File(outputPath);
        long sizeBytes = outFile.length();
        String sizeMB = String.format("%.2f MB", sizeBytes / 1048576.0);
        long totalSamples = sizeBytes / 2;
        double seconds = totalSamples / (48000.0 * 2);
        long mins = (long)(seconds / 60);
        long secs = (long)(seconds % 60);
        String duration = String.format("%d min %02d sec", mins, secs);
        DecimalFormat df = new DecimalFormat("0.#");
        String strengthStr = df.format(strength);
        String strengthTag = strength <= 1.0 ? " (Light)" : strength <= 2.0 ? " (Medium)" : " (Strong)";

        resultFileLabel.setText(outFile.getName());
        resultSizeLabel.setText(sizeMB);
        resultDurationLabel.setText(duration);
        resultStrengthLabel.setText(strengthStr + strengthTag);

        for (ActionListener al : openFolderButton.getActionListeners())
            openFolderButton.removeActionListener(al);
        openFolderButton.addActionListener(e -> {
            try { Desktop.getDesktop().open(outFile.getParentFile()); }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Could not open folder:\n" + ex.getMessage());
            }
        });

        resultsPanel.setVisible(true);
        frame.setSize(540, 680);
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }

    // ── UPDATE SLIDER COLOR ───────────────────────────────────────
    static void updateStrengthColor(double val) {
        if (val <= 1.0) strengthValueLabel.setForeground(new Color(39, 174, 96));
        else if (val <= 2.0) strengthValueLabel.setForeground(new Color(241, 196, 15));
        else strengthValueLabel.setForeground(new Color(231, 76, 60));
    }

    // ── HELPERS ───────────────────────────────────────────────────
    static JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 220, 235), 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(new Color(30, 78, 121));
        card.add(lbl);
        card.add(Box.createVerticalStrut(10));
        return card;
    }

    static JLabel makeResultKey(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    static JLabel makeResultVal(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl.setForeground(new Color(30, 30, 30));
        return lbl;
    }
}
