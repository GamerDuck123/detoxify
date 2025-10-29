package me.gamerduck.detoxify.api;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import me.gamerduck.detoxify.Platform;

import java.nio.LongBuffer;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class DetoxifyONNX {

    private final OrtEnvironment env;
    private final OrtSession session;
    private final HuggingFaceTokenizer tokenizer;
    private final String[] categories = {
            "toxicity",
            "severe_toxicity",
            "obscene",
            "threat",
            "insult",
            "identity_attack"
    };
    private final MessageSanitizer sanitizer;

    public DetoxifyONNX(String modelPath, Platform platform) throws Exception {
        env = OrtEnvironment.getEnvironment();
        session = env.createSession(modelPath, new OrtSession.SessionOptions());
        sanitizer = new MessageSanitizer(platform.mapsFolder().resolve("expansionsmap.txt"), platform.mapsFolder().resolve("symbolsmap.txt"), platform.mapsFolder().resolve("latinmap.txt"));
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            Path tokenizerPath = platform.libsFolder().resolve("tokenizer.json");
            tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath);
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }

    }

    public Map<String, Float> predict(String originalText) throws Exception {
        String text = sanitizer.sanitize(originalText);
        // Tokenize input
        var encoding = tokenizer.encode(text);
        long[] ids = encoding.getIds();
        long[] mask = encoding.getAttentionMask();

        long[] shape = new long[]{1, ids.length};

        OnnxTensor inputIds = OnnxTensor.createTensor(env, LongBuffer.wrap(ids), shape);
        OnnxTensor attention = OnnxTensor.createTensor(env, LongBuffer.wrap(mask), shape);

        Map<String, OnnxTensor> inputs = Map.of(
                "input_ids", inputIds,
                "attention_mask", attention
        );

        // Run the model
        try (OrtSession.Result result = session.run(inputs)) {
            float[][] output = (float[][]) result.get("outputs").get().getValue(); // shape: [1,7]

            Map<String, Float> scores = new LinkedHashMap<>();
            for (int i = 0; i < categories.length; i++) {
                scores.put(categories[i], output[0][i]);
            }
            return scores;
        } finally {
            inputIds.close();
            attention.close();
        }
    }

    public void close() throws Exception {
        session.close();
        env.close();
    }
}
