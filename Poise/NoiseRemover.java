public class NoiseRemover {

    // Estimate noise from the first few silent frames
    public static double[] estimateNoise(float[][] frames, int silentFrames) {
        int frameSize = frames[0].length;
        double[] noiseProfile = new double[frameSize];

        for (int f = 0; f < silentFrames; f++) {
            double[] real = new double[frameSize];
            for (int i = 0; i < frameSize; i++) real[i] = frames[f][i];

            double[][] result = FFTProcessor.fft(real.clone());
            double[] mag = FFTProcessor.magnitude(result[0], result[1]);

            for (int i = 0; i < frameSize; i++) {
                noiseProfile[i] += mag[i];
            }
        }

        // Average the noise across silent frames
        for (int i = 0; i < frameSize; i++) {
            noiseProfile[i] /= silentFrames;
        }

        return noiseProfile;
    }

    // Subtract noise from a single frame
    public static double[] cleanFrame(float[] frame, double[] noiseProfile, double strength) {
        int N = frame.length;
        double[] real = new double[N];
        for (int i = 0; i < N; i++) real[i] = frame[i];

        double[][] result = FFTProcessor.fft(real.clone());
        double[] re = result[0];
        double[] im = result[1];
        double[] mag = FFTProcessor.magnitude(re, im);

        for (int i = 0; i < N; i++) {
            double cleanMag = mag[i] - strength * noiseProfile[i];

            // Don't go below zero (spectral floor)
            if (cleanMag < 0) cleanMag = 0;

            // Rescale using original phase
            double scale = (mag[i] > 0) ? cleanMag / mag[i] : 0;
            re[i] *= scale;
            im[i] *= scale;
        }

        return FFTProcessor.ifft(re, im);
    }

    public static void main(String[] args) throws Exception {
        float[] audio = AudioReader.readWav("D:\\\\VSCode Infested\\\\practice\\\\java\\\\vct.wav");
        float[][] frames = FrameSlicer.slice(audio, 1024, 512);

        System.out.println("Estimating noise from first 10 frames...");
        double[] noiseProfile = estimateNoise(frames, 10);

        System.out.println("Cleaning frames...");
        double[][] cleanedFrames = new double[frames.length][];
        for (int i = 0; i < frames.length; i++) {
            cleanedFrames[i] = cleanFrame(frames[i], noiseProfile, 1.5);
        }

        System.out.println("Noise removal done!");
        System.out.println("Total cleaned frames: " + cleanedFrames.length);
        System.out.println("Sample from cleaned frame 100: " + cleanedFrames[100][5]);
    }
}