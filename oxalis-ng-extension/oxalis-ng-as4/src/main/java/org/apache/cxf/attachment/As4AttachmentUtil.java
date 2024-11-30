package org.apache.cxf.attachment;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.FileUtils;
import org.apache.cxf.message.Attachment;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class As4AttachmentUtil {

    private As4AttachmentUtil() {

    }

    static String getHeaderValue(List<String> v) {
        if (v != null && !v.isEmpty()) {
            return v.get(0);
        }
        return null;
    }

    static String getHeaderValue(List<String> v, String delim) {
        if (v != null && !v.isEmpty()) {
            return String.join(delim, v);
        }
        return null;
    }

    static String getHeader(Map<String, List<String>> headers, String h) {
        return getHeaderValue(headers.get(h));
    }

    static String getHeader(Map<String, List<String>> headers, String h, String delim) {
        return getHeaderValue(headers.get(h), delim);
    }

    public static Attachment createAttachment(InputStream stream, Map<String, List<String>> headers)
            throws IOException {

        String id = AttachmentUtil.cleanContentId(getHeader(headers, "Content-ID"));

        As4AttachmentImpl att = new As4AttachmentImpl(id);

        final String ct = getHeader(headers, "Content-Type");
        String cd = getHeader(headers, "Content-Disposition");
        String fileName = getContentDispositionFileName(cd);

        String encoding = null;

        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            String name = e.getKey();
            if ("Content-Transfer-Encoding".equalsIgnoreCase(name)) {
                encoding = getHeader(headers, name);
                if ("binary".equalsIgnoreCase(encoding)) {
                    att.setXOP(true);
                }
            }
            att.setHeader(name, getHeaderValue(e.getValue()));
        }
        if (encoding == null) {
            encoding = "binary";
        }
        InputStream ins = AttachmentUtil.decode(stream, encoding);
        if (ins != stream) {
            headers.remove("Content-Transfer-Encoding");
        }
        DataSource source = new As4AttachmentDataSource(ct, ins);
        if (!StringUtils.isEmpty(fileName)) {
            ((As4AttachmentDataSource) source).setName(FileUtils.stripPath(fileName));
        }
        att.setDataHandler(new DataHandler(source));
        return att;
    }

    static String getContentDispositionFileName(String cd) {
        if (StringUtils.isEmpty(cd)) {
            return null;
        }
        ContentDisposition c = new ContentDisposition(cd);
        String s = c.getParameter("filename");
        if (s == null) {
            s = c.getParameter("name");
        }
        return s;
    }
}

