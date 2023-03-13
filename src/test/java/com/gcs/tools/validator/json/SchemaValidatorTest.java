/****************************************************************************
 * FILE: SchemaValidatorTest.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.validator.json;





import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;



import java.io.IOException;



import org.junit.Ignore;
import org.junit.Test;



import com.github.fge.jsonschema.core.exceptions.ProcessingException;





public class SchemaValidatorTest
{

	@Ignore @Test
	public void test()
	{
		try
		{
			SchemaValidator validator = new SchemaValidator();
			validator.regiseter(Floor.class, "/exmple-json-schema.json");
			String json = "{\"name\":\"floor2\",\"id\":\"0ae298c5-2c17-49b9-a8ed-7681dd3e1743\"}";

			Floor f = validator.validate(Floor.class, json);
			assertNotNull(f);
			assertEquals("floor2", f.getName());
			assertEquals("0ae298c5-2c17-49b9-a8ed-7681dd3e1743", f.getId());
		}
		catch (ProcessingException ex_)
		{
			fail(ex_.toString());
		}
		catch (IOException ex_)
		{
			fail(ex_.toString());
		}
	}





	@Test
	public void testMissingJsonObject()
	{

		try
		{
			SchemaValidator validator = new SchemaValidator();
			validator.regiseter(Floor.class, "/exmple-json-schema.json");
			String json = "{\"name\":\"floor2\"}";

			Floor f = validator.validate(Floor.class, json);
			assertNotNull(f);
			assertEquals("floor2", f.getName());
			assertEquals("0ae298c5-2c17-49b9-a8ed-7681dd3e1743", f.getId());
		}
		catch (ProcessingException ex_)
		{
			fail(ex_.toString());
		}
		catch (IOException ex_)
		{
			fail(ex_.toString());
		}
	}

}
