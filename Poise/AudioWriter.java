import javax.sound.sampled.*;
import java.io.*;

public class AudioWriter {

    public static float[] overlapAdd(double[][] frames, int hopSize) {
        int frameSize = frames[0].length;
        int totalLength = (frames.length - 1) * hopSize + frameSize;
        float[] output = new float[totalLength];
        float[] counts  = new float[totalLength];

        for (int i = 0; i < frames.length; i++) {
            int start = i * hopSize;
            for (int j = 0; j < frameSize; j++) {
                output[start + j] += (float) frames[i][j];
                counts[start + j] += 1;
            }
        }

        // Normalize overlapping regions
        for (int i = 0; i < totalLength; i++) {
            if (counts[i] > 0) output[i] /= counts[i];
        }

        return output;
    }

    public static void saveWav(float[] audio, String filePath, float sampleRate, int channels) throws IOException {
        int numSamples = audio.length;
        byte[] bytes = new byte[numSamples * 2];

        for (int i = 0; i < numSamples; i++) {
            // Clamp to [-1, 1]
            float sample = Math.max(-1f, Math.min(1f, audio[i]));
            short s = (short)(sample * 32767);
            bytes[i * 2]     = (byte)(s & 0xFF);
            bytes[i * 2 + 1] = (byte)((s >> 8) & 0xFF);
        }

        AudioFormat format = new AudioFormat(sampleRate, 16, channels, true, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        AudioInputStream ais = new AudioInputStream(bais, format, numSamples);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(filePath));

        System.out.println("Saved to: " + filePath);
    }

    public static void main(String[] args) throws Exception {
        // Full pipeline run
        float[] audio = AudioReader.readWav("D:\\VSCode Infested\\practice\\java\\input.wav");
        float[][] frames = FrameSlicer.slice(audio, 1024, 512);

        double[] noiseProfile = NoiseRemover.estimateNoise(frames, 10);

        double[][] cleanedFrames = new double[frames.length][];
        for (int i = 0; i < frames.length; i++) {
            cleanedFrames[i] = NoiseRemover.cleanFrame(frames[i], noiseProfile, 1.5);
        }

        // Step 5 - stitch frames back together
        float[] cleanAudio = overlapAdd(cleanedFrames, 512);
        System.out.println("Stitched samples: " + cleanAudio.length);

        // Step 6 - save to file
        saveWav(cleanAudio, "D:\\\\VSCode Infested\\\\practice\\\\java\\\\vct.wav", 48000, 2);

        System.out.println("All done! Check output_clean.wav");
    }
}