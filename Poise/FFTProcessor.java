public class FFTProcessor {

    // FFT - converts audio samples into frequencies
    // Input: real number array (audio samples)
    // Output: two arrays - real part and imaginary part
    public static double[][] fft(double[] real) {
        int N = real.length;
        double[] imag = new double[N];

        // Bit-reversal permutation
        int j = 0;
        for (int i = 1; i < N; i++) {
            int bit = N >> 1;
            for (; (j & bit) != 0; bit >>= 1) j ^= bit;
            j ^= bit;
            if (i < j) {
                double temp = real[i]; real[i] = real[j]; real[j] = temp;
            }
        }

        // FFT butterfly operations
        for (int len = 2; len <= N; len <<= 1) {
            double angle = -2 * Math.PI / len;
            double wRe = Math.cos(angle);
            double wIm = Math.sin(angle);
            for (int i = 0; i < N; i += len) {
                double curRe = 1, curIm = 0;
                for (int k = 0; k < len / 2; k++) {
                    double uRe = real[i+k],      uIm = imag[i+k];
                    double vRe = real[i+k+len/2], vIm = imag[i+k+len/2];
                    double tRe = curRe*vRe - curIm*vIm;
                    double tIm = curRe*vIm + curIm*vRe;
                    real[i+k]        = uRe + tRe;
                    imag[i+k]        = uIm + tIm;
                    real[i+k+len/2]  = uRe - tRe;
                    imag[i+k+len/2]  = uIm - tIm;
                    double nextRe = curRe*wRe - curIm*wIm;
                    double nextIm = curRe*wIm + curIm*wRe;
                    curRe = nextRe;
                    curIm = nextIm;
                }
            }
        }

        return new double[][] { real, imag };
    }

    // IFFT - converts frequencies back to audio samples
    public static double[] ifft(double[] real, double[] imag) {
        int N = real.length;

        // Conjugate the imaginary part
        for (int i = 0; i < N; i++) imag[i] = -imag[i];

        // Run FFT again
        double[][] result = fft(real);
        double[] outReal = result[0];

        // Conjugate again and divide by N
        for (int i = 0; i < N; i++) outReal[i] /= N;

        return outReal;
    }

    // Get magnitude of each frequency bin
    public static double[] magnitude(double[] real, double[] imag) {
        double[] mag = new double[real.length];
        for (int i = 0; i < real.length; i++) {
            mag[i] = Math.sqrt(real[i]*real[i] + imag[i]*imag[i]);
        }
        return mag;
    }

    // Quick test
    public static void main(String[] args) throws Exception {
        float[] audio = AudioReader.readWav("D:\\\\VSCode Infested\\\\practice\\\\java\\\\vct.wav");
        float[][] frames = FrameSlicer.slice(audio, 1024, 512);

        // Test FFT on first frame
        double[] real = new double[1024];
        for (int i = 0; i < 1024; i++) real[i] = frames[0][i];

        double[][] result = fft(real);
        double[] mag = magnitude(result[0], result[1]);

        System.out.println("FFT done!");
        System.out.println("Frequency bins: " + mag.length);
        System.out.println("Bin 0 magnitude: " + mag[0]);
        System.out.println("Bin 1 magnitude: " + mag[1]);
        System.out.println("Bin 2 magnitude: " + mag[2]);

        // Test round-trip: FFT then IFFT should give back original
        double[] restored = ifft(result[0], result[1]);
        System.out.println("\nRound-trip check:");
        System.out.println("Original  sample[5]: " + frames[0][5]);
        System.out.println("Restored  sample[5]: " + (float)restored[5]);
    }
}
