import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;

public class NoiseServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Serve index.html
        server.createContext("/", exchange -> {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/") || path.equals("/index.html")) {
                File file = new File("public/index.html");
                byte[] response = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
                exchange.getResponseBody().close();
            }
        });

        // Handle noise removal
        server.createContext("/clean", exchange -> {
            if (!exchange.getRequestMethod().equals("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            try {
                // Read entire request body
                byte[] body = exchange.getRequestBody().readAllBytes();
                String contentType = exchange.getRequestHeaders()
                    .getFirst("Content-Type");

                // Parse boundary from content type
                String boundary = "--" + contentType.split("boundary=")[1].trim();

                // Parse multipart form
                String bodyStr = new String(body, "ISO-8859-1");
                String[] parts = bodyStr.split(boundary);

                byte[] fileBytes = null;
                String fileName = "input.wav";
                double strength = 1.5;

                for (String part : parts) {
                    if (part.contains("name=\"file\"")) {
                        // Extract filename
                        if (part.contains("filename=\"")) {
                            fileName = part.split("filename=\"")[1].split("\"")[0];
                        }
                        // Extract file bytes — after the double newline
                        int dataStart = part.indexOf("\r\n\r\n") + 4;
                        int dataEnd = part.lastIndexOf("\r\n");
                        if (dataStart > 4 && dataEnd > dataStart) {
                            String fileContent = part.substring(dataStart, dataEnd);
                            fileBytes = fileContent.getBytes("ISO-8859-1");
                        }
                    }
                    if (part.contains("name=\"strength\"")) {
                        int dataStart = part.indexOf("\r\n\r\n") + 4;
                        int dataEnd = part.lastIndexOf("\r\n");
                        if (dataStart > 4 && dataEnd > dataStart) {
                            strength = Double.parseDouble(
                                part.substring(dataStart, dataEnd).trim());
                        }
                    }
                }

                if (fileBytes == null) {
                    byte[] err = "No file found in request.".getBytes();
                    exchange.sendResponseHeaders(400, err.length);
                    exchange.getResponseBody().write(err);
                    exchange.getResponseBody().close();
                    return;
                }

                // Save uploaded file to temp
                Path tempDir = Files.createTempDirectory("noiseremover");
                Path inputPath = tempDir.resolve(fileName);
                Files.write(inputPath, fileBytes);

                // Convert MP4 to WAV if needed
                Path wavPath;
                if (fileName.toLowerCase().endsWith(".mp4")) {
                    wavPath = tempDir.resolve(
                        fileName.replace(".mp4", ".wav"));
                    WAV.convert(inputPath.toString(), wavPath.toString());
                } else {
                    wavPath = inputPath;
                }

                // Run noise removal pipeline
                float[] audio = AudioReader.readWav(wavPath.toString());
                float[][] frames = FrameSlicer.slice(audio, 1024, 512);
                double[] noiseProfile = NoiseRemover.estimateNoise(frames, 10);
                double[][] cleaned = new double[frames.length][];
                for (int i = 0; i < frames.length; i++)
                    cleaned[i] = NoiseRemover.cleanFrame(
                        frames[i], noiseProfile, strength);
                float[] cleanAudio = AudioWriter.overlapAdd(cleaned, 512);

                // Save output
                String cleanName = fileName.replaceAll("\\.[^.]+$", "")
                    + "_clean.wav";
                Path outputPath = tempDir.resolve(cleanName);
                AudioWriter.saveWav(cleanAudio, outputPath.toString(), 48000, 2);

                // Send file back
                byte[] responseBytes = Files.readAllBytes(outputPath);
                exchange.getResponseHeaders().set("Content-Type", "audio/wav");
                exchange.getResponseHeaders().set("Content-Disposition",
                    "attachment; filename=\"" + cleanName + "\"");
                exchange.sendResponseHeaders(200, responseBytes.length);
                exchange.getResponseBody().write(responseBytes);
                exchange.getResponseBody().close();

                System.out.println("Processed: " + cleanName
                    + " | Strength: " + strength);

            } catch (Exception e) {
                e.printStackTrace();
                byte[] err = ("Error: " + e.getMessage()).getBytes();
                exchange.sendResponseHeaders(500, err.length);
                exchange.getResponseBody().write(err);
                exchange.getResponseBody().close();
            }
        });

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Server running at http://localhost:8080");
    }
}