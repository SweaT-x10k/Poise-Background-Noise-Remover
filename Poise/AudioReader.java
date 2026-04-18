import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioReader {

    public static float[] readWav(String filePath) throws UnsupportedAudioFileException, IOException {
        File file = new File(filePath);

        try (AudioInputStream ais = AudioSystem.getAudioInputStream(file)) {

            AudioFormat format = ais.getFormat();
            System.out.println("Sample Rate : " + format.getSampleRate());
            System.out.println("Channels    : " + format.getChannels());
            System.out.println("Bit Depth   : " + format.getSampleSizeInBits());

            // Convert to PCM signed 16-bit if needed (e.g. mp3, ulaw, etc.)
            AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                format.getSampleRate(),
                16,
                format.getChannels(),
                format.getChannels() * 2,
                format.getSampleRate(),
                false  // little-endian
            );

            AudioInputStream pcmStream = AudioSystem.getAudioInputStream(targetFormat, ais);
            byte[] rawBytes = pcmStream.readAllBytes();

            return bytesToFloats(rawBytes);
        }
    }

    private static float[] bytesToFloats(byte[] bytes) {
        // Each sample is 2 bytes (16-bit PCM, little-endian)
        int numSamples = bytes.length / 2;
        float[] samples = new float[numSamples];

        for (int i = 0; i < numSamples; i++) {
            // Combine two bytes into a signed 16-bit integer
            int low  = bytes[i * 2]     & 0xFF;
            int high = bytes[i * 2 + 1] & 0xFF;
            short sample = (short) ((high << 8) | low);

            // Normalize to range [-1.0, 1.0]
            samples[i] = sample / 32768.0f;
        }

        return samples;
    }

    // Quick test
    public static void main(String[] args) throws Exception {

        float[] audio = readWav("D:\\New folder\\OneDrive\\Videos\\Captures\\film_clean.wav");
        System.out.println("Total samples loaded: " + audio.length);
        System.out.println("First 5 values: ");
        for (int i = 0; i < 5; i++) {
            System.out.printf("  sample[%d] = %.6f%n", i, audio[i]);
        }
    }
}