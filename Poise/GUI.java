import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

public class GUI {

    static JLabel statusLabel;
    static JProgressBar progressBar;
    static JTextField inputField;
    static JButton removeButton;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Background Noise Remover");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 250);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        // Title
        JLabel title = new JLabel("Background Noise Remover", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        frame.add(title, BorderLayout.NORTH);

        // Center panel
        JPanel center = new JPanel(new GridLayout(2, 1, 10, 10));
        center.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Input file row
        JPanel inputRow = new JPanel(new BorderLayout(5, 0));
        inputField = new JTextField("No file selected");
        inputField.setEditable(false);
        JButton browseInput = new JButton("Select MP4 File");
        browseInput.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("MP4 files", "mp4"));
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                inputField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(browseInput, BorderLayout.EAST);
        center.add(inputRow);

        // Remove button
        removeButton = new JButton("Convert + Remove Noise");
        removeButton.setFont(new Font("Arial", Font.BOLD, 14));
        removeButton.setBackground(new Color(70, 130, 180));
        removeButton.setForeground(Color.WHITE);
        removeButton.addActionListener(e -> processAudio(frame));
        center.add(removeButton);

        frame.add(center, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));
        statusLabel = new JLabel("Select an MP4 file to begin.", SwingConstants.CENTER);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        bottom.add(statusLabel, BorderLayout.NORTH);
        bottom.add(progressBar, BorderLayout.SOUTH);
        frame.add(bottom, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    static void processAudio(JFrame frame) {
        String inputPath = inputField.getText();

        if (inputPath.equals("No file selected")) {
            JOptionPane.showMessageDialog(frame, "Please select an MP4 file first.");
            return;
        }

        removeButton.setEnabled(false);
        progressBar.setValue(0);

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            protected Void doInBackground() throws Exception {

                publish("Converting MP4 to WAV...");
                progressBar.setValue(10);
                String wavPath = WAV.convert(inputPath);

                publish("Reading audio...");
                progressBar.setValue(25);
                float[] audio = AudioReader.readWav(wavPath);

                publish("Slicing frames...");
                progressBar.setValue(35);
                float[][] frames = FrameSlicer.slice(audio, 1024, 512);

                publish("Estimating noise...");
                progressBar.setValue(50);
                double[] noiseProfile = NoiseRemover.estimateNoise(frames, 10);

                publish("Removing noise...");
                progressBar.setValue(65);
                double[][] cleanedFrames = new double[frames.length][];
                for (int i = 0; i < frames.length; i++) {
                    cleanedFrames[i] = NoiseRemover.cleanFrame(frames[i], noiseProfile, 1.5);
                }

                publish("Saving clean audio...");
                progressBar.setValue(85);
                float[] cleanAudio = AudioWriter.overlapAdd(cleanedFrames, 512);
                String cleanOutput = inputPath.replace(".mp4", "_clean.wav");
                AudioWriter.saveWav(cleanAudio, cleanOutput, 48000, 2);

                progressBar.setValue(100);
                publish("Done! Saved as: " + cleanOutput);
                return null;
            }

            protected void process(java.util.List<String> chunks) {
                statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            protected void done() {
                removeButton.setEnabled(true);
                try {
                    get();
                    String cleanOutput = inputPath.replace(".mp4", "_clean.wav");
                    JOptionPane.showMessageDialog(frame,
                        "All done!\nClean audio saved to:\n" + cleanOutput);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}