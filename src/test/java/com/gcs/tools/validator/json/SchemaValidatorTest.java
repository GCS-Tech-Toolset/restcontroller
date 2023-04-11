/****************************************************************************
 * FILE: SchemaValidatorTest.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.validator.json;





import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;



import org.junit.Test;



import com.github.fge.jsonschema.core.exceptions.ProcessingException;



import lombok.SneakyThrows;





public class SchemaValidatorTest
{

	@Test @SneakyThrows
	public void test()
	{
		JsonSchemaValidator validator = new JsonSchemaValidator();
		validator.regiseter(Floor.class, "/exmple-json-schema.json");
		String json = "{\"name\":\"floor2\",\"id\":\"0ae298c5-2c17-49b9-a8ed-7681dd3e1743\"}";

		Floor f = validator.validate(Floor.class, json);
		assertNotNull(f);
		assertEquals("floor2", f.getName());
		assertEquals("0ae298c5-2c17-49b9-a8ed-7681dd3e1743", f.getId());
	}





	@Test(expected = ProcessingException.class) @SneakyThrows
	public void testMissingJsonObject()
	{
		JsonSchemaValidator validator = new JsonSchemaValidator();
		validator.regiseter(Floor.class, "/exmple-json-schema.json");
		String json = "{\"name\":\"floor2\"}";

		validator.validate(Floor.class, json);
		fail("expected exception:missing object");
	}





	@Test(expected = ProcessingException.class) @SneakyThrows
	public void testBadUuid()
	{
		JsonSchemaValidator validator = new JsonSchemaValidator();
		validator.regiseter(Floor.class, "/exmple-json-schema.json");
		String json = "{\"name\":\"floor2\",\"id\":\"0ae298c5\"}";

		validator.validate(Floor.class, json);
		fail("expected exception:uuid bad format");
	}





	@Test(expected = ProcessingException.class) @SneakyThrows
	public void testBadSized()
	{
		JsonSchemaValidator validator = new JsonSchemaValidator();
		validator.regiseter(Floor.class, "/exmple-json-schema.json");
		String json = "{\"name\":\"f\",\"id\":\"0ae298c5\"}";

		validator.validate(Floor.class, json);
		fail("expected exception:bad sized");
	}

}
