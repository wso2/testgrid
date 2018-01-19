/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.testgrid.web.bean;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

/**
 * Responsible for maintaining an input stream meta data.
 *
 * @since 1.0.0
 */
public class TruncatedInputStreamData {

    private static final int BYTES_PER_KB = 1024;
    private final boolean isTruncated;
    private final String completeInputStreamSize;
    private final String inputStreamContent;

    /**
     * Constructs an instance of a {@link TruncatedInputStreamData}.
     *
     * @param inputStream   input stream
     * @param kiloByteLimit limit of the input stream
     * @throws IOException thrown when error on determining the input stream size
     */
    public TruncatedInputStreamData(InputStream inputStream, int kiloByteLimit) throws IOException {
        byte[] inputStreamBytes = IOUtils.toByteArray(inputStream);
        int inputStreamSizeBytes = inputStreamBytes.length;
        this.isTruncated = inputStreamSizeBytes > BYTES_PER_KB * kiloByteLimit;
        this.completeInputStreamSize = humanReadableByteCount(inputStreamSizeBytes);
        this.inputStreamContent = new String(this.isTruncated ?
                                             Arrays.copyOf(inputStreamBytes, kiloByteLimit * 1024) :
                                             inputStreamBytes, StandardCharsets.UTF_8);
    }

    /**
     * Constructs an instance of a {@link TruncatedInputStreamData}. {@link TruncatedInputStreamData} instances
     * created using this constructor will not be truncated.
     *
     * @param inputStream input stream
     * @throws IOException thrown when error on determining the input stream size
     */
    public TruncatedInputStreamData(InputStream inputStream) throws IOException {
        byte[] inputStreamBytes = IOUtils.toByteArray(inputStream);
        int inputStreamSizeBytes = inputStreamBytes.length;
        this.isTruncated = false;
        this.completeInputStreamSize = humanReadableByteCount(inputStreamSizeBytes);
        this.inputStreamContent = new String(inputStreamBytes, StandardCharsets.UTF_8);
    }

    /**
     * Returns whether the input stream is actually truncated or not.
     *
     * @return {@code true} if the input stream is truncated, {@code false} otherwise
     */
    public boolean isTruncated() {
        return isTruncated;
    }

    /**
     * Returns the complete input stream size in human readable format.
     *
     * @return complete input stream size in human readable format
     */
    public String getCompleteInputStreamSize() {
        return completeInputStreamSize;
    }

    /**
     * Returns the content of the truncated input stream as a string.
     *
     * @return truncated input stream as a string
     */
    public String getInputStreamContent() {
        return inputStreamContent;
    }

    /**
     * Returns the number of bytes in a human readable format.
     *
     * @param bytes number of bytes
     * @return number of bytes in human readable format
     * <p>
     * Example:
     * <p>
     * 0:                      0 B
     * <p>
     * 27:                     27 B
     * <p>
     * 999:                    999 B
     * <p>
     * 1000:                   1000 B
     * <p>
     * 1023:                   1023 B
     * <p>
     * 1024:                   1.0 KiB
     * <p>
     * 1728:                   1.7 KiB
     * <p>
     * 110592:                 108.0 KiB
     * <p>
     * 7077888:                6.8 MiB
     * <p>
     * 452984832:              432.0 MiB
     * <p>
     * 28991029248:            27.0 GiB
     * <p>
     * 1855425871872:          1.7 TiB
     * <p>
     * 9223372036854775807:    8.0 EiB   (Long.MAX_VALUE)
     */
    private String humanReadableByteCount(long bytes) {
        if (bytes < BYTES_PER_KB) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(BYTES_PER_KB));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format(Locale.ENGLISH, "%.1f %sB", bytes / Math.pow(BYTES_PER_KB, exp), pre);
    }
}
