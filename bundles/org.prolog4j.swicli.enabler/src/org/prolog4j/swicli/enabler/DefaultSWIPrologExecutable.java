package org.prolog4j.swicli.enabler;

import org.prolog4j.swicli.SWIPrologExecutable;

public class DefaultSWIPrologExecutable  implements SWIPrologExecutable {

	public static final String DEFAULT_SWI_CALL = "swipl";
	
	@Override
    public String getPath() {
        return DEFAULT_SWI_CALL;
    }

}
