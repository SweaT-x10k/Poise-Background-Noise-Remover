import java.io.*;

public class WAV {

    public static String convert(String inputMp4) throws Exception {
        // Automatically create output name from input name
        // Example: video.mp4 → video.wav
        String outputWav = inputMp4.replace(".mp4", ".wav");

        String[] command = {
            "ffmpeg",
            "-i", inputMp4,
            "-vn",
            "-acodec", "pcm_s16le",
            "-ar", "48000",
            "-ac", "2",
            outputWav
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream())
        );
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println("Conversion done! Saved to: " + outputWav);
            return outputWav;
        } else {
            throw new Exception("FFmpeg conversion failed.");
        }
    }

    public static void main(String[] args) throws Exception {
        // Only provide input MP4 — output WAV is created automatically
        String inputMp4 = "D:\\New folder\\OneDrive\\Videos\\Captures\\film.mp4";

        System.out.println("Converting MP4 to WAV...");
        String outputWav = convert(inputMp4);

        // Automatically run noise remover on the converted WAV
        System.out.println("Running noise remover...");
        float[] audio = AudioReader.readWav(outputWav);
        float[][] frames = FrameSlicer.slice(audio, 1024, 512);
        double[] noiseProfile = NoiseRemover.estimateNoise(frames, 10);

        double[][] cleanedFrames = new double[frames.length][];
        for (int i = 0; i < frames.length; i++) {
            cleanedFrames[i] = NoiseRemover.cleanFrame(frames[i], noiseProfile, 1.5);
        }

        float[] cleanAudio = AudioWriter.overlapAdd(cleanedFrames, 512);
        String cleanOutput = inputMp4.replace(".mp4", "_clean.wav");
        AudioWriter.saveWav(cleanAudio, cleanOutput, 48000, 2);

        System.out.println("All done! Clean audio saved to: " + cleanOutput);
    }
}