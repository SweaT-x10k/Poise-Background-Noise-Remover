public class FrameSlicer {

    // Slice the audio into overlapping frames
    public static float[][] slice(float[] audio, int frameSize, int hopSize) {
        int numFrames = (audio.length - frameSize) / hopSize + 1;
        float[][] frames = new float[numFrames][frameSize];

        for (int i = 0; i < numFrames; i++) {
            int start = i * hopSize;
            for (int j = 0; j < frameSize; j++) {
                frames[i][j] = audio[start + j];
            }
        }
        return frames;
    }

    // Apply Hann window to a single frame (reduces edge glitches)
    public static float[] applyHannWindow(float[] frame) {
        int N = frame.length;
        float[] windowed = new float[N];
        for (int i = 0; i < N; i++) {
            double hann = 0.5 * (1 - Math.cos(2 * Math.PI * i / (N - 1)));
            windowed[i] = (float)(frame[i] * hann);
        }
        return windowed;
    }

    public static void main(String[] args) throws Exception {
        float[] audio = AudioReader.readWav("D:\\\\VSCode Infested\\\\practice\\\\java\\\\vct.wav");

        int frameSize = 1024;  // samples per frame
        int hopSize   = 512;   // 50% overlap

        float[][] frames = slice(audio, frameSize, hopSize);
        System.out.println("Total frames: " + frames.length);

        // Apply Hann window to each frame
        for (int i = 0; i < frames.length; i++) {
            frames[i] = applyHannWindow(frames[i]);
        }

        System.out.println("Windowing done. Frame 0, sample 0: " + frames[0][0]);
    }
}