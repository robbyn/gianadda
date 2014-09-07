package org.tastefuljava.gianadda.template;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.JdkLogChute;
import org.apache.velocity.tools.generic.ComparisonDateTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.MathTool;

public class TemplateEngine {
    private static final VelocityContext BASE_CONTEXT = createBaseContext();

    private final VelocityEngine engine;
    private final VelocityContext engineContext;

    public TemplateEngine(File templateDir, Map<String,?> params) {
        engine = new VelocityEngine();
        engine.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
        engine.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
        engine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new JdkLogChute());
        engine.setProperty(Velocity.RESOURCE_LOADER, "file");
        engine.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH,
                templateDir.getAbsolutePath());
        engine.init();
        engineContext = createContext(params, BASE_CONTEXT);
    }

    public void process(String template, Map<String,?> params, File outFile)
            throws IOException {
        try (OutputStream stream = new FileOutputStream(outFile);
                Writer out = new OutputStreamWriter(stream, "UTF-8")) {
            process(template, params, out);
        }
    }

    public void process(String template, Map<String,?> params, Writer out) {
        VelocityContext context = createContext(params, engineContext);
        engine.mergeTemplate(template, "UTF-8", context, out);
    }

    public String process(String template, Map<String,?> params) {
        StringWriter out = new StringWriter();
        process(template, params, out);
        return out.toString();
    }

    private static VelocityContext createContext(Map<String,?> params,
            VelocityContext base) {
        VelocityContext context = new VelocityContext(base);
        if (params != null) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                context.put(entry.getKey(), entry.getValue());
            }
        }
        return context;
    }

    private static VelocityContext createBaseContext() {
        VelocityContext context = new VelocityContext();
        context.put("math", new MathTool());
        context.put("esc", new EscapeTool());
        context.put("date", new ComparisonDateTool());
        return context;
    }
}
