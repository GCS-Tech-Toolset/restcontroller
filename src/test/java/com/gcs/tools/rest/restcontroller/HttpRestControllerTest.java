/**
 * Author: kgoldstein
 * Date: Feb 21, 2022
 * Terms: Expressly forbidden for use without written consent from the author
 */





package com.gcs.tools.rest.restcontroller;





import org.junit.Test;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class HttpRestControllerTest
{

    @Test
    public void test()
    {
        try
        {
            HttpRestController restCtrl = new HttpRestController("junit", 8081);
            restCtrl.register(new RestTest());
            restCtrl.start();
            restCtrl.join();
        }
        catch (Exception ex_)
        {
            _logger.error(ex_.toString(), ex_);
        }
    }

}
