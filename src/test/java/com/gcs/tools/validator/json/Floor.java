/****************************************************************************
 * FILE: Floor.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.validator.json;





import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;





@Data
@Builder
@Jacksonized
public class Floor
{
	private String	_id;
	private String	_name;

}
