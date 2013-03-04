package com.catify.processengine.management;

import java.io.File;
import java.io.IOException;

public interface ProcessImportService {

	/**
	 * Import a process definition.
	 *
	 * @param processDefinition the process definition
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void importProcessDefinition(File processDefinition)
			throws IOException;

	/**
	 * Removes a process definition.
	 *
	 * @param processDefinition the process definition
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void removeProcessDefinition(File processDefinition)
			throws IOException;

}