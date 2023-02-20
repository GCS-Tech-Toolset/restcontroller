/**
 * Author: kgoldstein
 * Date: Feb 20, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 */





package com.gcs.tools.rest;





import java.util.Optional;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class HttpUtils
{
    public static final String REFID = "X-Correlation-ID";

    public static String getRefId(final String refId_)
    {
        final String refId = Optional.ofNullable(refId_).orElse(Long.toString(System.nanoTime()));
        if (_logger.isTraceEnabled())
        {
            _logger.trace("new refid: [{}]", refId_);
        }
        return refId;
    }

}
