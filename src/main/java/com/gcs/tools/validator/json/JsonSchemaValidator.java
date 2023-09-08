/****************************************************************************
 * FILE: SchemaValidator.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.validator.json;





import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;



import javax.inject.Inject;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class JsonSchemaValidator
{
	private HashMap<Class<?>, JsonSchema>	_class2schema;
	private ObjectMapper					_mapper;


	@Inject
	public JsonSchemaValidator()
	{
		_class2schema = new HashMap<>();
		_mapper = new ObjectMapper();
	}





	public <T> void register(Class<T> classType_, String schemaFile_) throws IOException
	{
		try
		{
			final var url = this.getClass().getResource(schemaFile_);
			if (url == null)
			{
				throw new IOException("Schema not found:" + schemaFile_);
			}



			final JsonSchemaFactory factory = JsonSchemaFactory
					.newBuilder()
					.freeze();


			var uriAsString = url.toURI().toString();
			final JsonSchema schema = factory.getJsonSchema(uriAsString);

			_class2schema.put(classType_, schema);
			_logger.info("registered class type:{}, with schema file:{}", classType_.toString(), schemaFile_);
		}
		catch (ProcessingException | URISyntaxException ex_)
		{
			_logger.error(ex_.toString(), ex_);
			throw new IOException(ex_);
		}

	}





	public <T> T validate(Class<T> expected_, String str_) throws ProcessingException, IOException
	{
		final JsonSchema schema = _class2schema.get(expected_);
		if (schema == null)
		{
			throw new ProcessingException("Not schema loaded for:" + expected_.toString());
		}

		final JsonNode jsonNode = _mapper.readTree(str_);
		var rv = schema.validate(jsonNode);
		if (rv != null)
		{
			if (!rv.isSuccess())
			{
				_logger.debug("failed validation of json object");
				String errors = buildListOfErrors(rv.iterator());
				throw new ProcessingException(errors);
			}
		}
		else
		{
			throw new ProcessingException("Processing did not happen!");
		}

		return _mapper.readValue(str_, expected_);
	}





	private String buildListOfErrors(Iterator<ProcessingMessage> itr_)
	{
		StringBuilder buff = new StringBuilder();
		int cntr = 0;
		while (itr_.hasNext())
		{
			buff.append("[");
			buff.append(Integer.toString(cntr++)).append(":");
			buff.append(itr_.next().getMessage());
			buff.append("]");
		}
		return buff.toString();
	}
}
