/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal.directconnectivity;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.Error;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.internal.RMResources;
import com.microsoft.azure.cosmosdb.rx.internal.http.HttpHeaders;

import java.net.URL;
import java.util.Map;

public class ForbiddenException extends DocumentClientException {
    public ForbiddenException() {
        this(RMResources.Forbidden);
    }

    public ForbiddenException(Error error, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.FORBIDDEN, error, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    public ForbiddenException(String message) {
        this(message, null, null, null);
    }

    public ForbiddenException(String message, HttpHeaders headers, String requestUrlString) {
        this(message, null, headers, requestUrlString);
    }

    public ForbiddenException(String message, HttpHeaders headers, URL requestUrl) {
        this(message, headers, requestUrl != null ? requestUrl.toString() : null);
    }

    public ForbiddenException(Exception innerException) {
        this(RMResources.Forbidden, innerException, null, null);
    }

    public ForbiddenException(String message,
                              Exception innerException,
                              HttpHeaders headers,
                              String requestUrlString) {
        super(String.format("%s: %s", RMResources.Forbidden, message),
                innerException,
                HttpUtils.asMap(headers),
                HttpConstants.StatusCodes.FORBIDDEN,
                requestUrlString);
    }
}
