/****************************************************************************
 * FILE: HttpAsyncResponseManager.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.rest.restcontroller;





import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;



import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.Response;



import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;





@Slf4j
@RequiredArgsConstructor
public class HttpAsyncResponseManager<T>
{
    private BiMap<T, AsyncResponse> _asyncResponse = HashBiMap.create();
    private HttpTimeoutHandler      _toHandler     = new HttpTimeoutHandler();


    @NonNull @Getter private int      _timeout;
    @NonNull @Getter private TimeUnit _timeoutUnit;





    public void registerAsyncResponse(@NonNull T refId_, @NonNull AsyncResponse rsps_)
    {
        rsps_.setTimeout(1, TimeUnit.SECONDS);
        rsps_.setTimeoutHandler(_toHandler);
        _asyncResponse.put(refId_, rsps_);
        if (_logger.isTraceEnabled())
        {
            _logger.trace("[{}] registered time-out handler", refId_);
        }
    }





    public AsyncResponse removeAsyncResposne(@NonNull T refId_)
    {
        return _asyncResponse.remove(refId_);
    }





    public int getPendingAsyncSize()
    {
        return _asyncResponse.size();
    }





    @Override
    public String toString()
    {
        StringBuilder buff = new StringBuilder("HttpAsyncResponseManager");
        buff.append("[_timeout=").append(_timeout);
        buff.append(" " + _timeoutUnit).append("]: ");
        buff.append(new DecimalFormat("#,###").format(_asyncResponse.size()));
        buff.append(" pending requests");
        return buff.toString();
    }





    /**
     * the actual TimeOut handler
     */
    private final class HttpTimeoutHandler implements TimeoutHandler
    {

        @Override
        public void handleTimeout(AsyncResponse asyncResponse_)
        {
            final T ref = _asyncResponse.inverse().get(asyncResponse_);
            final var resp = Response.status(Response.Status.REQUEST_TIMEOUT).header("X-Correlation-ID", ref).build();
            asyncResponse_.resume(resp);
            _asyncResponse.remove(ref);
            _logger.warn("[{}] received timeout", ref);
        }

    }

}
