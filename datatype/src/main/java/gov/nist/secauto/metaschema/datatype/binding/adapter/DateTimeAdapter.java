package gov.nist.secauto.metaschema.datatype.binding.adapter;

import gov.nist.secauto.metaschema.datatype.DateTime;

public class DateTimeAdapter extends SimpleJavaTypeAdapter<DateTime> {

	@Override
	public DateTime parseValue(String value) {
		throw new UnsupportedOperationException();
	}

}
